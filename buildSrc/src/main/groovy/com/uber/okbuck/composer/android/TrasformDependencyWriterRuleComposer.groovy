package com.uber.okbuck.composer.android

import com.uber.okbuck.core.model.android.AndroidAppTarget
import com.uber.okbuck.core.model.base.RuleType
import com.uber.okbuck.core.util.FileUtil
import com.uber.okbuck.core.util.TransformUtil
import com.uber.okbuck.template.base.GenRule
import com.uber.okbuck.template.core.Rule
import org.gradle.api.Project

import java.nio.file.Files
import java.nio.file.StandardCopyOption

final class TrasformDependencyWriterRuleComposer extends AndroidBuckRuleComposer {

    static final String OPT_TRANSFORM_CLASS = "transform"
    static final String OPT_CONFIG_FILE = "configFile"
    static final String RUNNER_MAIN_CLASS = "com.uber.okbuck.transform.CliTransform"

    private TrasformDependencyWriterRuleComposer() {}

    static List<Rule> compose(AndroidAppTarget target) {
        return target.transforms.collect { Map<String, String> options ->
            compose(target, options)
        }
    }

    static Rule compose(AndroidAppTarget target, Map<String, String> options) {
        String transformClass = options.get(OPT_TRANSFORM_CLASS)
        String configFile = options.get(OPT_CONFIG_FILE)

        List<String> input = []
        if (configFile != null) {
            input.add(getTransformConfigRuleForFile(target.project, target.rootProject.file(configFile)))
        }

        String output = "\$OUT"
        List<String> cmds = [
                "echo \"#!/bin/bash\" > ${output};",
                "echo \"set -ex\" >> ${output};",

                "echo \"java " +

                        "-Dokbuck.inJarsDir=\"\\\$1\" " +
                        "-Dokbuck.outJarsDir=\"\\\$2\" " +
                        "-Dokbuck.androidBootClasspath=\"\\\$3\" " +

                        (configFile != null ? "-Dokbuck.configFile=\"\$SRCS\" " : "") +
                        (transformClass != null ? "-Dokbuck.transformClass=\"${transformClass}\" " : "") +

                        " -cp \$(location ${TransformUtil.TRANSFORM_RULE}) ${RUNNER_MAIN_CLASS}\" >> ${output};",

                "chmod +x ${output}"]

        String name = getTransformRuleName(target, options)

        return new GenRule()
                .inputs(input)
                .output("${name}_out")
                .bashCmds(cmds)
                .globSrcs(false)
                .executable(true)
                .name(name)
                .ruleType(RuleType.GENRULE.buckName)
    }

    static getTransformRuleName(AndroidAppTarget target, Map<String, String> options) {
        return transform(options.get(OPT_TRANSFORM_CLASS), target)
    }

    static String getTransformConfigRuleForFile(Project project, File config) {
        String path = getTransformFilePathForFile(project, config)
        File configFile = new File(project.rootDir, "${TransformUtil.TRANSFORM_CACHE}/${path}")
        try {
            Files.copy(config.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (IOException ignored) {
        }
        return "//${TransformUtil.TRANSFORM_CACHE}:${path}"
    }

    private static String getTransformFilePathForFile(Project project, File config) {
        return FileUtil.getRelativePath(project.rootDir, config).replaceAll('/', '_')
    }
}
