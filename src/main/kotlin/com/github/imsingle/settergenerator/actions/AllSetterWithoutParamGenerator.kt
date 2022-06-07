package com.github.imsingle.settergenerator.actions

import com.github.imsingle.settergenerator.utils.PsiClassUtils
import com.github.imsingle.settergenerator.utils.PsiDocumentUtils
import com.github.imsingle.settergenerator.utils.PsiToolUtils
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.util.IncorrectOperationException

class AllSetterWithoutParamGenerator : PsiElementBaseIntentionAction() {
    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val psiParent: Any = PsiTreeUtil.getParentOfType(
            element,
            PsiLocalVariable::class.java, PsiMethod::class.java
        ) ?: return
        if (psiParent is PsiLocalVariable) {
            val psiLocal = psiParent
            return handleWithLocalVariable(psiLocal, project, element)
        }
    }

    open fun handleWithLocalVariable(
        localVariable: PsiLocalVariable,
        project: Project, element: PsiElement
    ) {
        val parent = localVariable.parent as? PsiDeclarationStatement ?: return
        val psiClass = PsiTypesUtil.getPsiClass(localVariable.type)
        val generateName = localVariable.name
        val methodList: List<PsiMethod?> = PsiClassUtils.extractSetMethods(psiClass)
        if (methodList.isEmpty()) {
            return
        }
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val containingFile = element.containingFile
        val document = psiDocumentManager.getDocument(containingFile)
        val splitText: String = PsiToolUtils.calculateSplitText(document, parent.textOffset)
        val newImportList: MutableSet<String> = HashSet()
        val buildString: String = generateStringForNoParam(generateName, methodList, splitText)
        document!!.insertString(parent.textOffset + parent.text.length, buildString)
        PsiDocumentUtils.commitAndSaveDocument(psiDocumentManager, document)
        PsiToolUtils.addImportToFile(
            psiDocumentManager,
            containingFile as PsiJavaFile, document, newImportList
        )
        return
    }

    /**
     * 生成没有参数的方法
     */
    private fun generateStringForNoParam(
        generateName: String,
        methodList: List<PsiMethod?>, splitText: String
    ): String {
        val builder = StringBuilder()
        for (method in methodList) {
            builder.append(splitText)
            generateDefaultForOneMethod(generateName, builder, method)
        }
        return builder.toString()
    }

    private fun generateDefaultForOneMethod(
        generateName: String, builder: StringBuilder,
        method: PsiMethod?
    ) {
        builder.append(generateName + "." + method?.name + "();")
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiClass = PsiClassUtils.getLocalVariableContainingClass(element)


        return PsiClassUtils.checkClassHasValidSetMethod(psiClass)
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "生成所有set方法"
    }

    override fun getText(): @IntentionFamilyName String {
        return "生成所有set方法（无参）"
    }
}
