/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.gradle.pomf.task

import net.fabricmc.blendingjar.JarMerger
import net.fabricmc.gradle.pomf.PomfGradleExtension
import net.fabricmc.gradle.pomf.util.Constants
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Path

/**
 * The 'mergeJars' Gradle task.
 */
class MergeJarsTask extends DefaultTask {

    PomfGradleExtension extension

    @TaskAction
    void doTask() {
        Path versionPath = Constants.MINECRAFT_CACHE_DIRECTORY.resolve(extension.minecraft)
        Path minecraftClientLocal = versionPath.resolve('client.jar')
        Path minecraftServerLocal = versionPath.resolve('server.jar')
        Path minecraftMergedLocal = versionPath.resolve('merged.jar')

        FileInputStream client = new FileInputStream(minecraftClientLocal.toFile())
        FileInputStream server = new FileInputStream(minecraftServerLocal.toFile())
        FileOutputStream merged = new FileOutputStream(minecraftMergedLocal.toFile())

        JarMerger merger = new JarMerger(client, server, merged)
        merger.merge()

        merger.close()
        client.close()
        server.close()
        merged.close()
    }

}
