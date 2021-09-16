/*
* The MIT License (MIT)

* Copyright (c) 2015 Michal Tajchert

* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package com.naman14.timber.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import java.util.*

/**
 * Created by Michal Tajchert on 2015-06-04.
 */
@SuppressLint("StaticFieldLeak")
object Nammu {
    private val TAG = Nammu::class.java.simpleName
    private const val KEY_PREV_PERMISSIONS = "previous_permissions"
    private const val KEY_IGNORED_PERMISSIONS = "ignored_permissions"
    private var context: Context? = null
    private var sharedPreferences: SharedPreferences? = null
    private val permissionRequests = ArrayList<PermissionRequest>()
    @JvmStatic
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("pl.tajchert.runtimepermissionhelper", Context.MODE_PRIVATE)
        Nammu.context = context
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     */
    fun verifyPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Returns true if the Activity has access to given permissions.
     */
    fun hasPermission(activity: Activity, permission: String?): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }

    /**
     * Returns true if the Activity has access to a all given permission.
     */
    fun hasPermission(activity: Activity, permissions: Array<String?>): Boolean {
        for (permission in permissions) {
            if (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
                    } else {
                        TODO("VERSION.SDK_INT < M")
                    }) {
                return false
            }
        }
        return true
    }

    /*
     * If we override other methods, lets do it as well, and keep name same as it is already weird enough.
     * Returns true if we should show explanation why we need this permission.
     */
    fun shouldShowRequestPermissionRationale(activity: Activity, permissions: String?): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activity.shouldShowRequestPermissionRationale(permissions)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }

    fun askForPermission(activity: Activity, permission: String?, permissionCallback: PermissionCallback?) {
        askForPermission(activity, arrayOf(permission), permissionCallback)
    }

    fun askForPermission(activity: Activity, permissions: Array<String?>, permissionCallback: PermissionCallback?) {
        if (permissionCallback == null) {
            return
        }
        if (hasPermission(activity, permissions)) {
            permissionCallback.permissionGranted()
            return
        }
        val permissionRequest = PermissionRequest(ArrayList(Arrays.asList(*permissions)), permissionCallback)
        permissionRequests.add(permissionRequest)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activity.requestPermissions(permissions, permissionRequest.requestCode)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>?, grantResults: IntArray) {
        val requestResult = PermissionRequest(requestCode)
        if (permissionRequests.contains(requestResult)) {
            val permissionRequest = permissionRequests[permissionRequests.indexOf(requestResult)]
            if (verifyPermissions(grantResults)) {
                //Permission has been granted
                permissionRequest.permissionCallback.permissionGranted()
            } else {
                permissionRequest.permissionCallback.permissionRefused()
            }
            permissionRequests.remove(requestResult)
        }
        refreshMonitoredList()
    }
    //Permission monitoring part below//Group location
    //Group Calendar
    //Group Camera
    //Group Contacts
    //Group Microphone
    //Group Phone
    //Group Body sensors
    //Group SMS
    //Group Storage
    /**
     * Get list of currently granted permissions, without saving it inside Nammu
     *
     * @return currently granted permissions
     */
    val grantedPermissions: ArrayList<String>
        get() {
            if (context == null) {
                throw RuntimeException("Must call init() earlier")
            }
            val permissions = ArrayList<String>()
            val permissionsGranted = ArrayList<String>()
            //Group location
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            //Group Calendar
            permissions.add(Manifest.permission.WRITE_CALENDAR)
            permissions.add(Manifest.permission.READ_CALENDAR)
            //Group Camera
            permissions.add(Manifest.permission.CAMERA)
            //Group Contacts
            permissions.add(Manifest.permission.WRITE_CONTACTS)
            permissions.add(Manifest.permission.READ_CONTACTS)
            permissions.add(Manifest.permission.GET_ACCOUNTS)
            //Group Microphone
            permissions.add(Manifest.permission.RECORD_AUDIO)
            //Group Phone
            permissions.add(Manifest.permission.CALL_PHONE)
            permissions.add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions.add(Manifest.permission.READ_CALL_LOG)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions.add(Manifest.permission.WRITE_CALL_LOG)
            }
            permissions.add(Manifest.permission.ADD_VOICEMAIL)
            permissions.add(Manifest.permission.USE_SIP)
            permissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS)
            //Group Body sensors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                permissions.add(Manifest.permission.BODY_SENSORS)
            }
            //Group SMS
            permissions.add(Manifest.permission.SEND_SMS)
            permissions.add(Manifest.permission.READ_SMS)
            permissions.add(Manifest.permission.RECEIVE_SMS)
            permissions.add(Manifest.permission.RECEIVE_WAP_PUSH)
            permissions.add(Manifest.permission.RECEIVE_MMS)
            //Group Storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            for (permission in permissions) {
                if (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            context!!.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
                        } else {
                            TODO("VERSION.SDK_INT < M")
                        }) {
                    permissionsGranted.add(permission)
                }
            }
            return permissionsGranted
        }

    /**
     * Refresh currently granted permission list, and save it for later comparing using @permissionCompare()
     */
    fun refreshMonitoredList() {
        val permissions = grantedPermissions
        val set: MutableSet<String> = HashSet()
        for (perm in permissions) {
            set.add(perm)
        }
        sharedPreferences!!.edit().putStringSet(KEY_PREV_PERMISSIONS, set).apply()
    }

    /**
     * Get list of previous Permissions, from last refreshMonitoredList() call and they may be outdated,
     * use getGrantedPermissions() to get current
     */
    val previousPermissions: ArrayList<String>
        get() {
            val prevPermissions = ArrayList<String>()
            prevPermissions.addAll(sharedPreferences!!.getStringSet(KEY_PREV_PERMISSIONS, HashSet()))
            return prevPermissions
        }
    val ignoredPermissions: ArrayList<String>
        get() {
            val ignoredPermissions = ArrayList<String>()
            ignoredPermissions.addAll(sharedPreferences!!.getStringSet(KEY_IGNORED_PERMISSIONS, HashSet()))
            return ignoredPermissions
        }

    /**
     * Lets see if we already ignore this permission
     */
    fun isIgnoredPermission(permission: String?): Boolean {
        return if (permission == null) {
            false
        } else ignoredPermissions.contains(permission)
    }

    /**
     * Use to ignore to particular Permission - even if user will deny or add it we won't receive a callback.
     *
     * @param permission Permission to ignore
     */
    fun ignorePermission(permission: String) {
        if (!isIgnoredPermission(permission)) {
            val ignoredPermissions = ignoredPermissions
            ignoredPermissions.add(permission)
            val set: MutableSet<String> = HashSet()
            set.addAll(ignoredPermissions)
            sharedPreferences!!.edit().putStringSet(KEY_IGNORED_PERMISSIONS, set).apply()
        }
    }

    /**
     * Used to trigger comparing process - @permissionListener will be called each time Permission was revoked, or added (but only once).
     *
     * @param permissionListener Callback that handles all permission changes
     */
    fun permissionCompare(permissionListener: PermissionListener?) {
        if (context == null) {
            throw RuntimeException("Before comparing permissions you need to call Nammu.init(context)")
        }
        val previouslyGranted = previousPermissions
        val currentPermissions = grantedPermissions
        val ignoredPermissions = ignoredPermissions
        for (permission in ignoredPermissions) {
            if (previouslyGranted != null && !previouslyGranted.isEmpty()) {
                if (previouslyGranted.contains(permission)) {
                    previouslyGranted.remove(permission)
                }
            }
            if (currentPermissions != null && !currentPermissions.isEmpty()) {
                if (currentPermissions.contains(permission)) {
                    currentPermissions.remove(permission)
                }
            }
        }
        for (permission in currentPermissions) {
            if (previouslyGranted.contains(permission)) {
                //All is fine, was granted and still is
                previouslyGranted.remove(permission)
            } else {
                //We didn't have it last time
                if (permissionListener != null) {
                    permissionListener.permissionsChanged(permission)
                    permissionListener.permissionsGranted(permission)
                }
            }
        }
        if (previouslyGranted != null && !previouslyGranted.isEmpty()) {
            //Something was granted and removed
            for (permission in previouslyGranted) {
                if (permissionListener != null) {
                    permissionListener.permissionsChanged(permission)
                    permissionListener.permissionsRemoved(permission)
                }
            }
        }
        refreshMonitoredList()
    }

    /**
     * Not that needed method but if we override others it is good to keep same.
     */
    @JvmStatic
    fun checkPermission(permissionName: String?): Boolean {
        if (context == null) {
            throw RuntimeException("Before comparing permissions you need to call Nammu.init(context)")
        }
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PackageManager.PERMISSION_GRANTED == context!!.checkSelfPermission(permissionName)
        } else {
            TODO("VERSION.SDK_INT < M")
        }
    }
}