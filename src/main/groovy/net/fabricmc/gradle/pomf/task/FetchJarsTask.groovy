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

import com.google.common.hash.Hashing
import groovy.json.JsonSlurper
import net.fabricmc.gradle.pomf.PomfGradleExtension
import net.fabricmc.gradle.pomf.util.Constants
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * The 'fetchJars' Gradle task.
 */
class FetchJarsTask extends DefaultTask {

    PomfGradleExtension extension

    @TaskAction
    void doTask() {
        Path versionPath = Constants.CACHE_DIRECTORY.resolve(extension.minecraft)
        if (!Files.exists(versionPath) && !Files.isDirectory(versionPath)) {
            Files.createDirectory(versionPath)
        }

        Path versionManifestLocal = versionPath.resolve("${extension.minecraft}.json")
        Path minecraftClientLocal = versionPath.resolve('client.jar')
        Path minecraftServerLocal = versionPath.resolve('server.jar')

        logger.lifecycle(':downloading minecraft version manifest')
        URL versionManifestUrl = new URL("https://s3.amazonaws.com/Minecraft.Download/versions/${extension.minecraft}/${extension.minecraft}.json")
        Files.copy(versionManifestUrl.openStream(), versionManifestLocal, StandardCopyOption.REPLACE_EXISTING)

        def manifest = new JsonSlurper().parse(versionManifestLocal.toFile())

        if (Files.notExists(minecraftClientLocal) || !validateChecksum(minecraftClientLocal.toFile(), manifest.downloads.client.sha1)) {
            logger.lifecycle(":downloading minecraft client")
            Files.copy(new URL(manifest.downloads.client.url).openStream(), minecraftClientLocal, StandardCopyOption.REPLACE_EXISTING)
        }

        if (Files.notExists(minecraftServerLocal) || !validateChecksum(minecraftServerLocal.toFile(), manifest.downloads.server.sha1)) {
            logger.lifecycle(":downloading minecraft server")
            Files.copy(new URL(manifest.downloads.server.url).openStream(), minecraftServerLocal, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    static boolean validateChecksum(File file, String checksum) {
        if (file != null) {
            def hash = com.google.common.io.Files.hash(file, Hashing.sha1())
            def builder = new StringBuilder()
            hash.asBytes().each {
                builder.append(Integer.toString((it & 0xFF) + 0x100, 16).substring(1))
            }
            return builder.toString().equals(checksum)
        }
        return false
    }

}
