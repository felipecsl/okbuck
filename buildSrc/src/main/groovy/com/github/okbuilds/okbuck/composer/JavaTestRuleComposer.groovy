/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Piasy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.okbuilds.okbuck.composer

import com.github.okbuilds.okbuck.generator.RetroLambdaGenerator
import com.github.okbuilds.core.model.JavaLibTarget
import com.github.okbuilds.core.model.Target
import com.github.okbuilds.okbuck.rule.JavaTestRule

final class JavaTestRuleComposer {

    private JavaTestRuleComposer() {
        // no instance
    }

    static JavaTestRule compose(JavaLibTarget target) {
        List<String> deps = [
                ":src_${target.name}"
        ]

        deps.addAll(target.testCompileDeps.collect { String dep ->
            "//${dep.reverse().replaceFirst("/", ":").reverse()}"
        })

        deps.addAll(target.targetTestCompileDeps.collect { Target targetDep ->
            "//${targetDep.path}:src_${targetDep.name}"
        })

        Set<String> aptDeps = [] as Set
        aptDeps.addAll(target.aptDeps.collect { String dep ->
            "//${dep.reverse().replaceFirst("/", ":").reverse()}"
        })

        aptDeps.addAll(target.targetAptDeps.collect { Target targetDep ->
            "//${targetDep.path}:src_${targetDep.name}"
        })

        List<String> postprocessClassesCommands = []
        if (target.retrolambda) {
            postprocessClassesCommands.add(RetroLambdaGenerator.generate(target))
        }

        new JavaTestRule("src_${target.testName}", ["PUBLIC"], deps, target.testSources,
                target.annotationProcessors, aptDeps, target.sourceCompatibility,
                target.targetCompatibility, postprocessClassesCommands, target.jvmArgs)
    }
}