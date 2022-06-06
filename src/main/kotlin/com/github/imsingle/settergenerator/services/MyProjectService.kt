package com.github.imsingle.settergenerator.services

import com.intellij.openapi.project.Project
import com.github.imsingle.settergenerator.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
