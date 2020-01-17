/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.launcher.continuous

import org.gradle.integtests.fixtures.AbstractContinuousIntegrationTest
import org.gradle.util.TextUtil

class ContinuousWorkerDaemonServiceIntegrationTest extends AbstractContinuousIntegrationTest {
    def workerDaemonIdentityFileName = "build/workerId"
    def workerDaemonIdentityFile = file(workerDaemonIdentityFileName)
    def inputFile = file("inputFile")

    def setup() {
        workerDaemonIdentityFile.createFile()
        buildFile << """
            $taskTypeUsingWorkerDaemon
        """
        inputFile.text = "build 1"
    }

    def "reuses worker daemons across builds in a single session"() {
        buildFile << """
            task runInDaemon(type: DaemonTask)
        """

        when:
        succeeds("runInDaemon")

        then:
        outputContains("Runnable executed...")

        when:
        triggerNewBuild()

        then:
        succeeds()

        and:
        outputContains("Runnable executed...")

        and:
        assertSameDaemonWasUsed()
    }

    def triggerNewBuild() {
        inputFile.text = "build 2"
    }

    void assertSameDaemonWasUsed() {
        def workerDaemonSets = workerDaemonIdentityFile.readLines()
        assert workerDaemonSets.size() == 2
        assert workerDaemonSets[0].split(" ").size() > 0
        assert workerDaemonSets[0] == workerDaemonSets[1]
    }

    String getTaskTypeUsingWorkerDaemon() {
        return """
            import javax.inject.Inject
            import org.gradle.workers.WorkerExecutor
            import org.gradle.workers.internal.WorkerDaemonFactory

            class TestRunnable implements Runnable {
                void run() {
                    println "Runnable executed..."
                }
            }

            class DaemonTask extends DefaultTask {
                @InputFile
                File inputFile = new File("${TextUtil.normaliseFileAndLineSeparators(inputFile.absolutePath)}")

                @Inject
                WorkerExecutor getWorkerExecutor() {
                    throw new UnsupportedOperationException()
                }

                @TaskAction
                void runInDaemon() {
                    workerExecutor.submit(TestRunnable.class) {}
                    workerExecutor.await()
                    captureWorkerDaemons()
                }

                void captureWorkerDaemons() {
                    def workerDaemonIdentityFile = project.file("$workerDaemonIdentityFileName")
                    def daemonFactory = services.get(WorkerDaemonFactory)
                    workerDaemonIdentityFile << daemonFactory.clientsManager.allClients.collect { System.identityHashCode(it) }.sort().join(" ") + "\\n"
                }
            }
        """
    }
}
