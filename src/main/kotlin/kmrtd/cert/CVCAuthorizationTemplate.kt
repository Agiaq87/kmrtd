/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: CVCAuthorizationTemplate.java 1853 2021-06-26 18:13:26Z martijno $
 */
/*
 * Modified work Copyright (C) 2026 Alessandro Giaquinto
 * Kotlin port of JMRTD
 *
 * Licensed under LGPL 3.0
 */
package kmrtd.cert

import kmrtd.cert.support.Permission
import kmrtd.cert.support.Role
import org.ejbca.cvc.AccessRightEnum
import org.ejbca.cvc.AuthorizationRoleEnum
import org.ejbca.cvc.CVCAuthorizationTemplate

/**
 * Card verifiable certificate authorization template.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * @version $Revision: 1853 $
 */
data class CVCAuthorizationTemplate
/**
 * Constructs an authorization template.
 *
 * @param role        the role
 * @param accessRight the access rights
 */
    (
    /**
     * Returns the role.
     *
     * @return the role
     */
    val role: Role,

    /**
     * Returns the access rights.
     *
     * @return the access rights
     */
    val accessRight: Permission
) {
    companion object {
        /**
         * Translates a permission to an EJBCA typed equivalent permission.
         * 
         * @param permission a permission
         * @return the EJBCA typed equivalent of the given permission
         */
        fun fromPermission(permission: Permission): AccessRightEnum =
            try {
                when (permission) {
                    Permission.READ_ACCESS_NONE -> AccessRightEnum.READ_ACCESS_NONE
                    Permission.READ_ACCESS_DG3 -> AccessRightEnum.READ_ACCESS_DG3
                    Permission.READ_ACCESS_DG4 -> AccessRightEnum.READ_ACCESS_DG4
                    Permission.READ_ACCESS_DG3_AND_DG4 -> AccessRightEnum.READ_ACCESS_DG3_AND_DG4
                    else -> throw IllegalArgumentException("Error getting permission for " + permission)
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Error getting permission from AuthZ template", e)
            }

        /**
         * Translates a role to an EJBCA typed equivalent role.
         * 
         * @param role a role
         * @return the EJBCA typed equivalent role
         */
        fun fromRole(role: Role): AuthorizationRoleEnum =
            try {
                when (role) {
                    Role.CVCA -> AuthorizationRoleEnum.CVCA
                    Role.DV_D -> AuthorizationRoleEnum.DV_D
                    Role.DV_F -> AuthorizationRoleEnum.DV_F
                    Role.IS -> AuthorizationRoleEnum.IS
                    else -> throw IllegalArgumentException("Error getting role from AuthZ template $role")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Error getting role from AuthZ template", e)
            }

        /**
         * Translates an EJBCA typed role to a role.
         * 
         * @param template the EJBCA typed role
         * @return the equivalent role
         */
        private fun toRole(template: CVCAuthorizationTemplate): Role =
            try {
                when (template.authorizationField.getRole()) {
                    AuthorizationRoleEnum.CVCA -> Role.CVCA
                    AuthorizationRoleEnum.DV_D -> Role.DV_D
                    AuthorizationRoleEnum.DV_F -> Role.DV_F
                    AuthorizationRoleEnum.IS -> Role.IS
                    else -> throw IllegalArgumentException("Unsupported role")
                }
            } catch (nsfe: NoSuchFieldException) {
                throw IllegalArgumentException("Error getting role from AuthZ template", nsfe)
            }

        /**
         * Translates an EJBCA typed permission to an equivalent permission.
         * 
         * @param template the EJBCA typed permission
         * @return the equivalent permission
         */
        private fun toPermission(template: CVCAuthorizationTemplate): Permission =
            try {
                when (template.authorizationField.getAccessRight()) {
                    AccessRightEnum.READ_ACCESS_NONE -> Permission.READ_ACCESS_NONE
                    AccessRightEnum.READ_ACCESS_DG3 -> Permission.READ_ACCESS_DG3
                    AccessRightEnum.READ_ACCESS_DG4 -> Permission.READ_ACCESS_DG4
                    AccessRightEnum.READ_ACCESS_DG3_AND_DG4 -> Permission.READ_ACCESS_DG3_AND_DG4
                    else -> throw IllegalArgumentException("Unsupported access right")
                }
            } catch (nsfe: NoSuchFieldException) {
                throw IllegalArgumentException("Unsupported access right", nsfe)
            }

        /**
         * Factory method for creating an authorization template.
         *
         * @param template [org.ejbca.cvc.CVCAuthorizationTemplate] the authZ template to wrap
         * @throws IllegalArgumentException if cannot decode role or permission
         */
        fun decode(template: CVCAuthorizationTemplate): kmrtd.cert.CVCAuthorizationTemplate {
            return CVCAuthorizationTemplate(
                toRole(template),
                toPermission(template)
            )
        }
    }
}
