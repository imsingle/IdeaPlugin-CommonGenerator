package com.github.imsingle.settergenerator.actions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.IncorrectOperationException

class NewObjectGenerator : PsiElementBaseIntentionAction() {

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        // User|
        val cond1 = element is PsiIdentifier && element.parent is PsiJavaCodeReferenceElement;
        // User|;
        val cond2 = element is PsiJavaToken && element.text == ";" && element.prevSibling is PsiReferenceExpression
        val typeName = if(cond1) element.text else element.prevSibling.text
        val varName = typeName.replaceRange(0, 1, ""+typeName.get(0).toLowerCase())

        val containingFile = element.containingFile
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        val document = psiDocumentManager.getDocument(containingFile)
        var statement = " $varName = new ${typeName}()"
        if (cond1 && element.parent?.nextSibling?.text != ";") {
            // 后面没有;
           statement += ";"
        }
        val offset = if(cond1) element.endOffset else element.startOffset
        document!!.insertString(offset, statement)
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        // User|
        val cond1 = element is PsiIdentifier && element.parent is PsiJavaCodeReferenceElement;
        // User|;
        val cond2 = element is PsiJavaToken && element.text == ";" && element.prevSibling is PsiReferenceExpression
        return cond1 || cond2
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "new对象"
    }

    override fun getText(): @IntentionFamilyName String {
        return "new对象"
    }
}
