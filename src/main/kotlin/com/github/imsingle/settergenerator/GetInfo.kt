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
package com.github.imsingle.settergenerator

import com.intellij.psi.PsiMethod

/**
 * @Author bruce.ge
 * @Date 2017/1/19
 * @Description
 */
class GetInfo {
    var paramName: String? = null
    var getMethods: List<PsiMethod>? = null
    var nameToMethodMap: Map<String, PsiMethod>? = null
}
