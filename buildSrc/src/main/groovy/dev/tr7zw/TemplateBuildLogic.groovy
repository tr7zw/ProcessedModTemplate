package dev.tr7zw

import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.publish.maven.MavenPublication

class TemplateBuildLogic {

    static void configureStonecutter(Project project, String modBrand) {
        project.stonecutter {
            constants.match(
                modBrand,
                "fabric",
                "neoforge",
                "forge",
            )

            File replacements = new File(project.rootProject.rootDir, "replacements.gradle")
            if (replacements.exists()) {
                project.apply from: replacements.absolutePath
            }

            File globalReplacements = new File(project.rootProject.rootDir, "globalReplacements.gradle")
            if (globalReplacements.exists()) {
                project.apply from: globalReplacements.absolutePath
            }
        }
    }

    static void configurePmd(Project project, String modBrand, String mcVersion) {
        if (modBrand == 'fabric' && project.stonecutter.eval(mcVersion, '>= 1.21.11')) {
            project.apply plugin: 'pmd'
            project.pmd {
                consoleOutput = true
                toolVersion = '7.16.0'
                rulesMinimumPriority = 5
                ruleSets = []
                ruleSetFiles = project.files("${project.rootProject.rootDir}/pmd-rules.xml")
                ignoreFailures = true
                threads = Runtime.runtime.availableProcessors()
            }
        }
    }

    static void configureEclipseAndConfigurations(Project project) {
        project.eclipse {
            delegate.project {
                name "${project.parent.name}-${project.name}"
                comment "Gradle project ${project.parent.name}-${project.name}"
            }
        }

        project.configurations {
            inc
            implementation.extendsFrom inc
        }

        project.configurations.all {
            resolutionStrategy.cacheChangingModulesFor 1, 'minutes'
            resolutionStrategy.cacheDynamicVersionsFor 1, 'minutes'
        }
    }

    static Map<String, Integer> defaultPackFormats() {
        return [
            '1.8.9'   : 1,
            '1.12.2'  : 3,
            '1.15.0'  : 5,
            '1.16.5'  : 6,
            '1.17.1'  : 7,
            '1.18.2'  : 8,
            '1.19.2'  : 9,
            '1.19.3'  : 12,
            '1.19.4'  : 13,
            '1.20.1'  : 15,
            '1.20.2'  : 18,
            '1.20.3'  : 21,
            '1.20.4'  : 22,
            '1.20.5'  : 32,
            '1.20.6'  : 32,
            '1.21.1'  : 34,
            '1.21.2'  : 42,
            '1.21.3'  : 42,
            '1.21.4'  : 46,
            '1.21.5'  : 55,
            '1.21.6'  : 63,
            '1.21.7'  : 63,
            '1.21.8'  : 63,
            '1.21.9'  : 69,
            '1.21.10' : 69,
            '1.21.11' : 75,
            '26.1'    : 84,
            '26.1.2'    : 84,
            '26.2-pre-5': 88,
        ]
    }

    static void configureProcessResources(Project project, String modBrand, String mcVersion, Map<String, Integer> versionsMap, Map<String, Object> replaceProperties, String licenseName) {
        project.tasks.named('processResources').configure {
            dependsOn(project.tasks.named('stonecutterGenerate'))
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE

            Integer packFormat = versionsMap[mcVersion]
            println "Using Minecraft version: ${mcVersion} with pack format number: ${packFormat}"

            Map<String, Object> expandedProperties = new LinkedHashMap<>(replaceProperties)
            expandedProperties.pack_format_number = packFormat

            inputs.properties(expandedProperties)

            filesMatching(['fabric.mod.json', 'META-INF/mods.toml', 'META-INF/neoforge.mods.toml', '**/pack.mcmeta']) {
                expand expandedProperties + [project: project]
            }

            from "${project.rootDir}/${licenseName}"

            if (modBrand != 'fabric') {
                exclude {
                    it.file.name == 'fabric.mod.json'
                }
            }
        }
    }

    static void configureTestTasks(Project project, String modBrand, String mcVersion) {
        project.tasks.named('test').configure {
            onlyIf { modBrand == 'fabric' && project.stonecutter.eval(mcVersion, '>= 1.18.0') }
            useJUnitPlatform()
        }

        project.tasks.named('compileTestJava').configure {
            onlyIf { modBrand == 'fabric' && project.stonecutter.eval(mcVersion, '>= 1.18.0') }
        }
    }

    static void configureSourceSetsAndRunCleanup(Project project) {
        project.sourceSets.each {
            def dir = project.layout.buildDirectory.dir("sourcesSets/${it.name}")
            it.output.resourcesDir = dir
            it.java.destinationDirectory = dir
        }

        project.afterEvaluate { p ->
            p.tasks.withType(JavaExec).configureEach { task ->
                if (task.name.startsWith('runClient')) {
                    task.doFirst {
                        File binDir = p.file('bin')
                        if (binDir.exists()) {
                            p.ant.delete(dir: binDir, failonerror: false)
                        }
                    }
                }
            }
        }
    }

    static void configureCoordinatesRepositoriesAndJava(Project project, String modBrand, JavaVersion javaVersion) {
        project.base {
            archivesName = project.rootProject.archives_base_name
        }
        project.version = "${modBrand}-${project.rootProject.mod_version}-mc${project.minecraft_version}${project.gradle.ext.versionMetadata}"
        project.group = project.rootProject.maven_group

        project.repositories {
            maven {
                name = 'tr7zw-proxy'
                url 'https://maven.tr7zw.dev/repository/maven-public/'
            }
            maven {
                name = 'tr7zw-tmp'
                url 'https://maven.tr7zw.dev/repository/maven-tmp/'
            }
            maven {
                name = 'Modrinth'
                url = 'https://api.modrinth.com/maven'
                content {
                    includeGroup 'maven.modrinth'
                }
            }
            maven {
                name = 'NeoForged'
                url = 'https://maven.neoforged.net/releases'
            }
            maven {
                url 'https://cursemaven.com'
            }
            mavenLocal()
        }

        project.tasks.withType(JavaCompile).configureEach {
            options.encoding = 'UTF-8'
        }

        project.java {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }

    static void applyOptionalDependencyScript(Project project) {
        File gradleFile = new File(project.projectDir, 'dependencies.gradle')
        if (gradleFile.exists()) {
            project.apply from: gradleFile.absolutePath
            return
        }

        File gradleKtsFile = new File(project.projectDir, 'dependencies.gradle.kts')
        if (gradleKtsFile.exists()) {
            project.apply from: gradleKtsFile.absolutePath
        }
    }

    static void configurePublishing(Project project, String modBrand, String artifactTaskName) {
        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    groupId = project.group
                    artifactId = '$name$'
                    version = "${project.rootProject.mod_version}-${project.minecraft_version}-${modBrand}-SNAPSHOT"

                    artifact(project.tasks.named(artifactTaskName).map { it.archiveFile.get().asFile }) {
                        extension = 'jar'
                    }
                }
            }
            repositories {
                maven {
                    name = 'NexusSnapshots'
                    url = project.uri('https://maven.tr7zw.dev/repository/maven-snapshots/')

                    credentials {
                        username = project.hasProperty('nexusUsername') ? project.nexusUsername : System.getenv('NEXUS_USERNAME')
                        password = project.hasProperty('nexusPassword') ? project.nexusPassword : System.getenv('NEXUS_PASSWORD')
                    }
                }
            }
        }

        project.tasks.named('publishMods').configure {
            onlyIf {
                !("${project.minecraft_version}".contains('w') || "${project.minecraft_version}".contains('rc') || "${project.minecraft_version}".contains('pre'))
            }
        }
    }

    static void configureLoomJarShadowPipeline(Project project, String modBrand) {
        project.tasks.register('extractManifest', Copy) {
            dependsOn project.tasks.named('jar')

            from {
                project.zipTree(project.tasks.named('jar').get().archiveFile)
            }

            include 'META-INF/MANIFEST.MF'
            include 'fabric.mod.json'

            into project.layout.buildDirectory.dir('tmp')
        }

        project.shadowJar {
            duplicatesStrategy(DuplicatesStrategy.FAIL)
            dependsOn(project.tasks.named('jar'))
            dependsOn(project.tasks.named('extractManifest'))
            manifest.from(project.layout.buildDirectory.file('tmp/META-INF/MANIFEST.MF'))
            mainSpec.sourcePaths.clear()
            from(project.zipTree(project.tasks.named('jar').get().archiveFile))

            configurations = [project.configurations.inc]
            relocate 'dev.tr7zw.util', '$relocationpackage$.util'
            relocate 'dev.tr7zw.config', '$relocationpackage$.config'
            relocate 'io.sentry', 'dev.tr7zw.lib.sentry'
            doLast {
                project.delete(project.tasks.named('jar').get().archiveFile)
                archiveFile.get().asFile.renameTo(project.tasks.named('jar').get().archiveFile.get().asFile)
            }
        }

        project.jar {
            if (modBrand == 'forge') {
                manifest {
                    attributes(
                        'MixinConfigs': '$id$.mixins.json'
                    )
                }
            }
            finalizedBy(project.tasks.named('shadowJar'))
        }
    }

    static void addLoomMinecraftAndMappings(Project project, String mcVersion, boolean modernLoom) {
        project.dependencies {
            minecraft "com.mojang:minecraft:${project.minecraft_version}"

            if (modernLoom && project.stonecutter.eval(project.minecraft_version.toString(), '>= 26.0')) {
                project.loom.noIntermediateMappings()
            } else {
                mappings project.loom.layered() {
                    officialMojangMappings()
                    if (project.stonecutter.eval(mcVersion, '>= 1.21.5')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_21_5}:${project.rootProject.parchment_version_21_5}@zip")
                    } else if (project.stonecutter.eval(mcVersion, '>= 1.21.4')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_21_4}:${project.rootProject.parchment_version_21_4}@zip")
                    } else if (project.stonecutter.eval(mcVersion, '>= 1.21.0')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_21}:${project.rootProject.parchment_version_21}@zip")
                    } else if (project.stonecutter.eval(mcVersion, '>= 1.20.4')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_20_4}:${project.rootProject.parchment_version_20_4}@zip")
                    } else if (project.stonecutter.eval(mcVersion, '>= 1.20.2')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_20_2}:${project.rootProject.parchment_version_20_2}@zip")
                    } else if (project.stonecutter.eval(mcVersion, '>= 1.20.1')) {
                        parchment("org.parchmentmc.data:parchment-${project.rootProject.parchment_mc_version_20_1}:${project.rootProject.parchment_version_20_1}@zip")
                    }
                }
            }
        }
    }

    static void addLombokDependencies(Project project) {
        project.dependencies {
            compileOnly "org.projectlombok:lombok:${project.lombok_version}"
            annotationProcessor "org.projectlombok:lombok:${project.lombok_version}"

            testCompileOnly "org.projectlombok:lombok:${project.lombok_version}"
            testAnnotationProcessor "org.projectlombok:lombok:${project.lombok_version}"
        }
    }

    static void addFabricRuntimeDependencies(Project project, String mcVersion, boolean modernLoom) {
        project.dependencies {
            if (modernLoom && project.stonecutter.eval(project.minecraft_version.toString(), '>= 26.0')) {
                implementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
                implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
                if (project.hasProperty('mod_menu_compile_only')) {
                    compileOnly "maven.modrinth:modmenu:${project.mod_menu_release}"
                } else {
                    implementation "maven.modrinth:modmenu:${project.mod_menu_release}"
                }
            } else {
                modImplementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
                modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
                if (project.hasProperty('mod_menu_compile_only')) {
                    modCompileOnly "maven.modrinth:modmenu:${project.mod_menu_release}"
                } else if (project.stonecutter.eval(mcVersion, '= 1.21.5')) {
                    modImplementation 'terraformers:modmenu:14.0.0-rc2@jar'
                } else {
                    modImplementation "maven.modrinth:modmenu:${project.mod_menu_release}"
                }
            }
        }
    }

    static void addFabricTestDependencies(Project project) {
        project.dependencies {
            testImplementation "net.fabricmc:fabric-loader-junit:${project.rootProject.fabric_loader_version}"
            testImplementation 'org.objenesis:objenesis:3.4'
        }
    }

    static void addOptionalTemplateLibraries(Project project, String modBrand, String implementationConfiguration, String forgeMixinConfiguration) {
        project.dependencies {
            <addTRenderLib>
            <includeLibs>
            include("dev.tr7zw:TRender:${project.rootProject.trender_version}-${project.minecraft_version}-${modBrand}-SNAPSHOT@jar") {
                changing = true
            }
            </includeLibs>
            "${implementationConfiguration}"("dev.tr7zw:TRender:${project.rootProject.trender_version}-${project.minecraft_version}-${modBrand}-SNAPSHOT@jar")
            </addTRenderLib>

            <addTRansitionLib>
            <includeLibs>
            include("dev.tr7zw:TRansition:${project.rootProject.transition_version}-${project.minecraft_version}-${modBrand}-SNAPSHOT@jar") {
                changing = true
            }
            </includeLibs>
            "${implementationConfiguration}"("dev.tr7zw:TRansition:${project.rootProject.transition_version}-${project.minecraft_version}-${modBrand}-SNAPSHOT@jar")
            </addTRansitionLib>

            <mixinextras>
            <includeLibs>
            if (modBrand == 'forge') {
                compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:${project.rootProject.mixin_extras_version}"))
                include("io.github.llamalad7:mixinextras-forge:${project.rootProject.mixin_extras_version}")
                "${forgeMixinConfiguration}"("io.github.llamalad7:mixinextras-forge:${project.rootProject.mixin_extras_version}")
            }
            </includeLibs>
            </mixinextras>
        }
    }

    static void configurePublishModsBase(def publishModsTarget, Project project, String modBrand, Object artifactFile) {
        publishModsTarget.file = artifactFile
        publishModsTarget.changelog = project.file("${project.rootProject.rootDir}/changelog.md").exists() ? project.file("${project.rootProject.rootDir}/changelog.md").text : 'Allan, please add details! (tr messed up the changelog)'
        publishModsTarget.version = "${project.rootProject.mod_version}"
        publishModsTarget.type = publishModsTarget.STABLE
        publishModsTarget.displayName = "${project.rootProject.mod_version}-${project.minecraft_version} - " + (modBrand == 'fabric' ? 'Fabric' : modBrand == 'forge' ? 'Forge' : 'NeoForge')
        publishModsTarget.modLoaders.add(modBrand)
        publishModsTarget.dryRun = project.providers.environmentVariable('MODRINTH_TOKEN').getOrNull() == null || project.providers.environmentVariable('CURSEFORGE_TOKEN').getOrNull() == null
        publishModsTarget.maxRetries = 6
    }

    static void configureCurseforgeDefaults(def curseforgeTarget, Project project, String modBrand, String mcVersion, boolean java25) {
        curseforgeTarget.minecraftVersions.add("${project.minecraft_version}")
        curseforgeTarget.clientRequired = true
        curseforgeTarget.serverRequired = false
        if (java25) {
            curseforgeTarget.javaVersions.add(JavaVersion.VERSION_25)
        } else if (project.stonecutter.eval(mcVersion, '>= 1.20.5')) {
            curseforgeTarget.javaVersions.add(JavaVersion.VERSION_21)
        } else if (project.stonecutter.eval(mcVersion, '>= 1.18.0')) {
            curseforgeTarget.javaVersions.add(JavaVersion.VERSION_17)
        } else {
            curseforgeTarget.javaVersions.add(JavaVersion.VERSION_1_8)
        }
        addExtraGameVersions(curseforgeTarget, project)
        if (modBrand == 'fabric') {
            curseforgeTarget.requires('fabric-api')
        }
    }

    static void configureModrinthDefaults(def modrinthTarget, Project project, String modBrand) {
        modrinthTarget.minecraftVersions.add("${project.minecraft_version}")
        addExtraGameVersions(modrinthTarget, project)
        if (modBrand == 'fabric') {
            modrinthTarget.requires('fabric-api')
        }
    }

    static void addExtraGameVersions(def publishTarget, Project project) {
        if (project.minecraft_version == '1.21') {
            publishTarget.minecraftVersions.add('1.21.1')
        }
        if (project.minecraft_version == '1.21.6') {
            publishTarget.minecraftVersions.add('1.21.7')
            publishTarget.minecraftVersions.add('1.21.8')
        }
        if (project.minecraft_version == '26.1') {
            publishTarget.minecraftVersions.add('26.1.1')
            publishTarget.minecraftVersions.add('26.1.2')
        }
    }

}
