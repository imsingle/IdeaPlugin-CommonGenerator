/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */
package com.github.imsingle.settergenerator.utils

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.PsiMethod
import com.github.imsingle.settergenerator.utils.PsiClassUtils
import java.util.ArrayList
import java.util.HashSet

/**
 * @Author bruce.ge
 * @Date 2017/1/30
 * @Description
 */
object PsiClassUtils {
    fun getLocalVariableContainingClass(element: PsiElement): PsiClass? {
        val psiParent = PsiTreeUtil.getParentOfType(
            element,
            PsiLocalVariable::class.java
        ) ?: return null
        val psiLocal = psiParent
        return if (psiLocal.parent !is PsiDeclarationStatement) {
            null
        } else PsiTypesUtil.getPsiClass(psiLocal.type)
    }

    fun isNotSystemClass(psiClass: PsiClass?): Boolean {
        if (psiClass == null) {
            return false
        }
        val qualifiedName = psiClass.qualifiedName
        val okJavaSet: MutableSet<String?> = HashSet()
        okJavaSet.add("java.util.Map.Entry")
        if (okJavaSet.contains(qualifiedName)) {
            return true
        }
        return if (qualifiedName == null || qualifiedName.startsWith("java.")) {
            false
        } else true
    }

    fun isValidSetMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty("public") &&
                !m.hasModifierProperty("static") &&
                (m.name.startsWith("set") || m.name.startsWith("with"))
    }

    fun isValidGetMethod(m: PsiMethod): Boolean {
        return m.hasModifierProperty("public") && !m.hasModifierProperty("static") &&
                (m.name.startsWith("get") || m.name.startsWith("is"))
    }

    fun addSetMethodToList(psiClass: PsiClass?, methodList: MutableList<PsiMethod?>) {
        val methods = psiClass!!.methods
        for (method in methods) {
            if (isValidSetMethod(method)) {
                methodList.add(method)
            }
        }
    }

    fun addGettMethodToList(psiClass: PsiClass?, methodList: MutableList<PsiMethod?>) {
        val methods = psiClass!!.methods
        for (method in methods) {
            if (isValidGetMethod(method)) {
                methodList.add(method)
            }
        }
    }

    fun extractSetMethods(psiClass: PsiClass?): List<PsiMethod?> {
        var psiClass = psiClass
        val methodList: MutableList<PsiMethod?> = ArrayList()
        while (isNotSystemClass(psiClass)) {
            addSetMethodToList(psiClass, methodList)
            psiClass = psiClass!!.superClass
        }
        return methodList
    }

    fun extractGetMethod(psiClass: PsiClass?): List<PsiMethod?> {
        var psiClass = psiClass
        val methodList: MutableList<PsiMethod?> = ArrayList()
        while (isNotSystemClass(psiClass)) {
            addGettMethodToList(psiClass, methodList)
            psiClass = psiClass!!.superClass
        }
        return methodList
    }

    fun checkClassHasValidSetMethod(psiClass: PsiClass?): Boolean {
        var psiClass: PsiClass? = psiClass ?: return false
        while (isNotSystemClass(psiClass)) {
            for (m in psiClass!!.methods) {
                if (isValidSetMethod(m)) {
                    return true
                }
            }
            psiClass = psiClass.superClass
        }
        return false
    }

    fun checkClasHasValidGetMethod(psiClass: PsiClass?): Boolean {
        var psiClass: PsiClass? = psiClass ?: return false
        while (isNotSystemClass(psiClass)) {
            for (m in psiClass!!.methods) {
                if (isValidGetMethod(m)) {
                    return true
                }
            }
            psiClass = psiClass.superClass
        }
        return false
    }
}
