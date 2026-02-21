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

import org.ejbca.cvc.AccessRightEnum
import org.ejbca.cvc.AuthorizationRoleEnum
import org.ejbca.cvc.CVCAuthorizationTemplate

/**
 * Card verifiable certificate authorization template.
 * 
 * @author The JMRTD team (info@jmrtd.org)
 * 
 * @version $Revision: 1853 $
 */
class CVCAuthorizationTemplate {
    /**
     * The authorization role.
     * 
     * @author The JMRTD team (info@jmrtd.org)
     * 
     * @version $Revision: 1853 $
     */
    enum class Role(value: Int) {
        /** Certificate authority.  */
        CVCA(0xC0),

        /** Document verifier domestic.  */
        DV_D(0x80),

        /** Document verifier foreign.  */
        DV_F(0x40),

        /** Inspection system.  */
        IS(0x00);

        /**
         * Returns the value as a bitmap.
         * 
         * @return a bitmap
         */
        val value: Byte = value.toByte()
    }

    /**
     * The authorization permission.
     * 
     * @author The JMRTD team (info@jmrtd.org)
     * 
     * @version $Revision: 1853 $
     */
    enum class Permission(value: Int) {
        /** No read access.  */
        READ_ACCESS_NONE(0x00),

        /** Read access to DG3.  */
        READ_ACCESS_DG3(0x01),

        /** Read access to DG4.  */
        READ_ACCESS_DG4(0x02),

        /** Read access to DG3 and DG4.  */
        READ_ACCESS_DG3_AND_DG4(0x03);

        /**
         * Returns the tag as a bitmap.
         * 
         * @return a bitmap
         */
        val value: Byte = value.toByte()

        /**
         * Whether this permission implies the other permission.
         * 
         * @param other some other permission
         * 
         * @return a boolean
         */
        fun implies(other: Permission?): Boolean {
            return when (this) {
                READ_ACCESS_NONE -> other == READ_ACCESS_NONE
                READ_ACCESS_DG3 -> other == READ_ACCESS_DG3
                READ_ACCESS_DG4 -> other == READ_ACCESS_DG4
                READ_ACCESS_DG3_AND_DG4 -> other == READ_ACCESS_DG3 || other == READ_ACCESS_DG4 || other == READ_ACCESS_DG3_AND_DG4
                else -> false
            }
        }
    }

    /**
     * Returns the role.
     * 
     * @return the role
     */
    val role: Role

    /**
     * Returns the access rights.
     * 
     * @return the access rights
     */
    val accessRight: Permission

    /**
     * Constructs an authorization template based on an EJBCA authorization template.
     * 
     * @param template the authZ template to wrap
     */
    protected constructor(template: CVCAuthorizationTemplate) {
        this.role = toRole(template)
        this.accessRight = toPermission(template)
    }

    /**
     * Constructs an authorization template.
     * 
     * @param role the role
     * @param accessRight the access rights
     */
    constructor(role: Role, accessRight: Permission) {
        this.role = role
        this.accessRight = accessRight
    }

    /**
     * Returns a textual representation of this authorization template.
     * 
     * @return a textual representation of this authorization template
     */
    override fun toString(): String {
        return role.toString() + accessRight.toString()
    }

    /**
     * Checks equality.
     * 
     * @param otherObj the other object
     * 
     * @return whether the other object is equal to this object
     */
    override fun equals(otherObj: Any?): Boolean {
        if (otherObj == null) {
            return false
        }
        if (otherObj === this) {
            return true
        }
        if (this.javaClass != otherObj.javaClass) {
            return false
        }

        val otherTemplate = otherObj as kmrtd.cert.CVCAuthorizationTemplate
        return this.role == otherTemplate.role && this.accessRight == otherTemplate.accessRight
    }

    /**
     * Returns a hash code of this object.
     * 
     * @return the hash code
     */
    override fun hashCode(): Int {
        return 2 * role.value + 3 * accessRight.value + 61
    }

    companion object {
        /**
         * Translates a permission to an EJBCA typed equivalent permission.
         * 
         * @param permission a permission
         * 
         * @return the EJBCA typed equivalent of the given permission
         */
        fun fromPermission(permission: Permission): AccessRightEnum {
            return try {
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
        }

        /**
         * Translates a role to an EJBCA typed equivalent role.
         * 
         * @param role a role
         * 
         * @return the EJBCA typed equivalent role
         */
        fun fromRole(role: Role): AuthorizationRoleEnum {
            try {
                return when (role) {
                    Role.CVCA -> AuthorizationRoleEnum.CVCA
                    Role.DV_D -> AuthorizationRoleEnum.DV_D
                    Role.DV_F -> AuthorizationRoleEnum.DV_F
                    Role.IS -> AuthorizationRoleEnum.IS
                    else -> throw IllegalArgumentException("Error getting role from AuthZ template $role")
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("Error getting role from AuthZ template", e)
            }
        }

        /**
         * Translates an EJBCA typed role to a role.
         * 
         * @param template the EJBCA typed role
         * 
         * @return the equivalent role
         */
        private fun toRole(template: CVCAuthorizationTemplate): Role {
            try {
                return when (val role = template.authorizationField.getRole()) {
                    AuthorizationRoleEnum.CVCA -> Role.CVCA
                    AuthorizationRoleEnum.DV_D -> Role.DV_D
                    AuthorizationRoleEnum.DV_F -> Role.DV_F
                    AuthorizationRoleEnum.IS -> Role.IS
                    else -> throw IllegalArgumentException("Unsupported role $role")
                }
            } catch (nsfe: NoSuchFieldException) {
                throw IllegalArgumentException("Error getting role from AuthZ template", nsfe)
            }
        }

        /**
         * Translates an EJBCA typed permission to an equivalent permission.
         * 
         * @param template the EJBCA typed permission
         * 
         * @return the equivalent permission
         */
        private fun toPermission(template: CVCAuthorizationTemplate): Permission {
            try {
                return when (val accessRight = template.authorizationField.getAccessRight()) {
                    AccessRightEnum.READ_ACCESS_NONE -> Permission.READ_ACCESS_NONE
                    AccessRightEnum.READ_ACCESS_DG3 -> Permission.READ_ACCESS_DG3
                    AccessRightEnum.READ_ACCESS_DG4 -> Permission.READ_ACCESS_DG4
                    AccessRightEnum.READ_ACCESS_DG3_AND_DG4 -> Permission.READ_ACCESS_DG3_AND_DG4
                    else -> throw IllegalArgumentException("Unsupported access right $accessRight")
                }
            } catch (nsfe: NoSuchFieldException) {
                throw IllegalArgumentException("Unsupported access right", nsfe)
            }
        }
    }
}
