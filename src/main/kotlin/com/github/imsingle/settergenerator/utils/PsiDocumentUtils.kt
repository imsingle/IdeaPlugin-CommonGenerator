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

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManager

/**
 * Created by bruce.ge on 2016/12/23.
 */
object PsiDocumentUtils {
    @JvmStatic
    fun commitAndSaveDocument(psiDocumentManager: PsiDocumentManager, document: Document?) {
        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document)
            psiDocumentManager.commitDocument(document)
            FileDocumentManager.getInstance().saveDocument(document)
        }
    }
}
