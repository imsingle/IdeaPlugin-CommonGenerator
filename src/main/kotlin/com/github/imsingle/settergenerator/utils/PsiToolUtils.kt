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

import com.github.imsingle.settergenerator.Parameters
import com.github.imsingle.settergenerator.RealParam
import com.github.imsingle.settergenerator.utils.PsiDocumentUtils.commitAndSaveDocument
import com.intellij.openapi.editor.Document
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.util.PsiTypesUtil
import org.apache.commons.lang.StringUtils


/**
 * @Author bruce.ge
 * @Date 2017/1/28
 * @Description
 */
object PsiToolUtils {
    fun checkGuavaExist(project: Project?, element: PsiElement): Boolean {
        val moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element) ?: return false
        val listss = PsiShortNamesCache.getInstance(project)
            .getClassesByName("Lists", GlobalSearchScope.moduleRuntimeScope(moduleForPsiElement, false))
        for (psiClass in listss) {
            if (psiClass.qualifiedName == "com.google.common.collect.Lists");
            return true
        }
        return false
    }

    fun addImportToFile(
        psiDocumentManager: PsiDocumentManager?,
        containingFile: PsiJavaFile,
        document: Document,
        newImportList: MutableSet<String>
    ) {
        if (newImportList.size > 0) {
            val iterator = newImportList.iterator()
            while (iterator.hasNext()) {
                val u = iterator.next()
                if (u.startsWith("java.lang") == true) {
                    iterator.remove()
                }
            }
        }
        if (newImportList.size > 0) {
            val importStatements = containingFile.importList!!.importStatements
            val containedSet: MutableSet<String?> = HashSet()
            for (s in importStatements) {
                containedSet.add(s.qualifiedName)
            }
            val newImportText = StringBuilder()
            for (newImport in newImportList) {
                if (!containedSet.contains(newImport)) {
                    newImportText.append("\nimport $newImport;")
                }
            }
            val packageStatement = containingFile.packageStatement
            var start = 0
            if (packageStatement != null) {
                start = packageStatement.textLength + packageStatement.textOffset
            }
            val insertText = newImportText.toString()
            if (StringUtils.isNotBlank(insertText)) {
                document.insertString(start, insertText)
                commitAndSaveDocument(psiDocumentManager!!, document)
            }
        }
    }
//
//    fun lowerStart(name: String): String {
//        return name.substring(0, 1).lowercase(Locale.getDefault()) + name.substring(1)
//    }

    //    public static WrapInfo extractWrappInfo(String typeFullName) {
    //        int u = typeFullName.indexOf("<");
    //        if (u == -1) {
    //            return null;
    //        }
    //        WrapInfo info = new WrapInfo();
    //        String fullName = typeFullName.substring(0, u);
    //        info.setQualifyTypeName(fullName);
    //        info.setShortTypeName(extractShortName(fullName));
    //        return info;
    //    }
    private fun extractShortName(fullName: String): String {
        return fullName.substring(fullName.lastIndexOf(".") + 1)
    }

    fun calculateSplitText(document: Document?, statementOffset: Int): String {
        var splitText = ""
        var cur = statementOffset
        var text = document?.getText(TextRange(cur - 1, cur))
        while (text == " " || text == "\t") {
            splitText = text + splitText
            cur--
            if (cur < 1) {
                break
            }
            text = document?.getText(TextRange(cur - 1, cur))
        }
        return "\n" + splitText
    }

    fun extractParamInfo(psiType: PsiType): Parameters {
        val typeFullName = psiType.canonicalText
        val info = Parameters()
        info.returnType = PsiTypesUtil.getPsiClass(psiType)
        val u = typeFullName.indexOf("<")
        if (u == -1) {
            val realParamList: MutableList<RealParam> = ArrayList()
            val real = RealParam()
            real.realName = extractShortName(typeFullName)
            real.realPackage = typeFullName
            realParamList.add(real)
            info.params = realParamList
        } else {
            val collectPart = typeFullName.substring(0, u)
            val realClassPart = typeFullName.substring(u + 1, typeFullName.length - 1)
            info.collectName = extractShortName(collectPart)
            info.collectPackage = collectPart
            val realClasses = realClassPart.split(",").toTypedArray()
            val realParamList: MutableList<RealParam> = ArrayList()
            if (realClasses.size > 0) {
                for (realClass in realClasses) {
                    val param = RealParam()
                    param.realPackage = realClass
                    param.realName = extractShortName(realClass)
                    realParamList.add(param)
                }
            }
            info.params = realParamList
        }
        return info
    }
}
