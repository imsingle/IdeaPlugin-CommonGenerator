package com.github.imsingle.settergenerator.actions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.IncorrectOperationException

class NewObjectGenerator : PsiElementBaseIntentionAction() {

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val cond1 = cond1(element)
        val typeName = if(cond1) element.text else element.prevSibling.text
        val varName = typeName.decapitalize()

        val containingFile = element.containingFile
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(containingFile)
        var statement = " $varName = new ${typeName}();"

        val offset = if(cond1) element.endOffset else element.startOffset
        document!!.insertString(offset, statement)
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        return cond1(element) || cond2(element)
    }

    /**
     * Use|r
     */
    fun cond1(element: PsiElement):Boolean {
        return PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) != null
                && element.elementType.toString() == "IDENTIFIER"
                // 排除掉方法入参里的类型
//                && PsiTreeUtil.getParentOfType(element, PsiParameter::class.java) == null
    }

    /**
     * User|  User |
     */
    fun cond2(element: PsiElement):Boolean {
        if (element.elementType != TokenType.WHITE_SPACE) {
            return false
        }
        return PsiTreeUtil.getPrevSiblingOfType(element, PsiExpressionStatement::class.java) != null
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "new Object()"
    }

    override fun getText(): @IntentionFamilyName String {
        return "new Object()"
    }
}
