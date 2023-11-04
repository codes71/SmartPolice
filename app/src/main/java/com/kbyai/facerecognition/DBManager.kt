package com.kbyai.facerecognition

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream



class DBManager(context: Context?) : SQLiteOpenHelper(context, "mydb", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // TODO Auto-generated method stub
        db.execSQL(
            "create table person " +
                    "(name text, face blob, templates blob)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS person")
        onCreate(db)
    }

    fun insertPerson(name: String?, face: Bitmap, templates: ByteArray?) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        face.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val faceJpg = byteArrayOutputStream.toByteArray()
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", name)
        contentValues.put("face", faceJpg)
        contentValues.put("templates", templates)
        db.insert("person", null, contentValues)
        personList.add(Person(name, face, templates))
    }

    fun deletePerson(name: String): Int {
        var i = 0
        while (i < personList.size) {
            if (personList[i].name === name) {
                personList.removeAt(i)
                i--
            }
            i++
        }
        val db = this.writableDatabase
        return db.delete(
            "person",
            "name = ? ", arrayOf(name)
        )
    }

    fun clearDB(): Int {
        personList.clear()
        val db = this.writableDatabase
        db.execSQL("delete from person")
        return 0
    }

    fun loadPerson() {
        personList.clear()
        val db = this.readableDatabase
        val res = db.rawQuery("select * from person", null)
        res.moveToFirst()
        while (res.isAfterLast == false) {
            val name = res.getString(res.getColumnIndex("name"))
            val faceJpg = res.getBlob(res.getColumnIndex("face"))
            val templates = res.getBlob(res.getColumnIndex("templates"))
            val face = BitmapFactory.decodeByteArray(faceJpg, 0, faceJpg.size)
            val person = Person(name, face, templates)
            personList.add(person)
            res.moveToNext()
        }
    }

    companion object {
        @JvmField
        var personList = ArrayList<Person>()
    }
}