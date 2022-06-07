package com.github.imsingle.settergenerator.actions

import com.github.imsingle.settergenerator.Parameters
import com.github.imsingle.settergenerator.config.GeneratorConstants
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
import java.util.*

class AllSetterWithParamGenerator : PsiElementBaseIntentionAction() {
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
        val buildString: String = generateStringForNoParam(generateName, methodList, splitText, newImportList)
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
        methodList: List<PsiMethod?>, splitText: String, newImportList: MutableSet<String>
    ): String {
        val builder = StringBuilder()
        for (method in methodList) {
            builder.append(splitText)
            generateDefaultForOneMethod(generateName, builder, method, newImportList)
        }
        return builder.toString()
    }

    private fun generateDefaultForOneMethod(
        generateName: String, builder: StringBuilder,
        method: PsiMethod?, newImportList: MutableSet<String>
    ) {
        val parameters = method!!.parameterList.parameters
        builder.append(generateName).append(".").append(method.name).append("(")

        val length = parameters.size
        var h = 0
        for (parameter in parameters) {
            h++
            val parameterClassType: String = parameter.type.canonicalText
            var defaultValue: String? = GeneratorConstants.typeGeneratedMap.get(parameterClassType)
            if (defaultValue != null) {
                builder.append(defaultValue)
                val importType: String? =
                    GeneratorConstants.typeGeneratedImport.get(parameterClassType)
                if (importType != null) {
                    newImportList.add(importType)
                }
            } else {
                // shall check which import list to use.
                val paramInfo: Parameters = PsiToolUtils.extractParamInfo(parameter.type)

                if (paramInfo.collectName != null) {
                    val defaultImpl: String? =
                        GeneratorConstants.defaultCollections.get(paramInfo.collectName)
                    if (defaultImpl != null) {
                        GeneratorConstants.appendCollectNotEmpty(
                            builder, paramInfo,
                            defaultImpl, newImportList
                        )
                        newImportList.add(
                            GeneratorConstants.defaultImportMap.get(paramInfo.collectName)!!
                        )
                    } else {
                        GeneratorConstants.appendCollectNotEmpty(
                            builder, paramInfo,
                            paramInfo.collectName, newImportList
                        )
                        newImportList.add(paramInfo.collectPackage!!)
                    }
                } else {
                    // may be could get the construct of the class. get the
                    // constructor of the class.
                    val realPackage: String? = paramInfo.params?.get(0)?.realPackage
                    // todo could add more to the default package values.
                    val s: String? = GeneratorConstants.defaultPacakgeValues.get(realPackage)
                    val psiClassOfParameter = PsiTypesUtil.getPsiClass(parameter.type)
                    if (s != null) {
                        builder.append(s)
                    } else if (psiClassOfParameter != null && psiClassOfParameter.isEnum) {
                        val allFields = psiClassOfParameter.allFields
                        Arrays.stream(allFields).findFirst().ifPresent { field: PsiField ->
                            builder.append(
                                psiClassOfParameter.name
                            ).append(".").append(field.name)
                        }
                    } else {
                        builder.append("new ").append(paramInfo.params?.get(0)?.realName).append("()")
                    }
                    newImportList.add(realPackage!!)
                }
            }
            if (h != length) {
                builder.append(",")
            }
        }
        builder.append(");")
    }

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiClass = PsiClassUtils.getLocalVariableContainingClass(element)

        return PsiClassUtils.checkClassHasValidSetMethod(psiClass)
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "生成所有set方法"
    }

    override fun getText(): @IntentionFamilyName String {
        return "生成所有set方法（默认参数）"
    }
}
