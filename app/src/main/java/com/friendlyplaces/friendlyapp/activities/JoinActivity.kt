package com.friendlyplaces.friendlyapp.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.IdRes
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Base64
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.friendlyplaces.friendlyapp.R
import kotlinx.android.synthetic.main.activity_join.*
import java.io.ByteArrayOutputStream
import java.io.IOException

/*
0 gay
1 lesb
2 bisex
3 transgender
4 pansex
5 hetero
6 othters
 */
class JoinActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (view?.getId()) {
            R.id.sp_sex_orientation -> {
                sp_sex_orientation.setSelection(position)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.getId()) {
            R.id.register_button -> {
                if (checkearDatosNotEmpty()) {
                    (this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view.getWindowToken(), 0)
                    //AQUI HO GUARDARIA TOT AL FIREBASE I FARIA EL INTENT
                }
            }
            R.id.imageJoin -> {
                view.startAnimation(AlphaAnimation(1f, 0.85f))
                checkPermisionCamera()
            }
        }
    }

    fun checkearDatosNotEmpty(): Boolean {
        //todo: faltan els spinners
        val trimName = et_name.text.toString().trim { it <= ' ' }
        val trimDescription = et_description.text.toString().trim({ it <= ' ' })
        //PODRÍA posar que el primer objecte del array fos select orientation i aixi si es 0 doncss es que no esta inicialitzat
        //chekear el spinner

        var requiredConditions = true

        if (trimName.isEmpty()) {
            et_name.error = "Campo obligatorio"
            et_name.requestFocus()
            requiredConditions = false
        }
        if (trimDescription.isEmpty()) {
            et_description.setError("Campo obligatorio")
            et_description.requestFocus()
            requiredConditions = false
        }
        if (sp_sex_orientation.selectedItemPosition == 0){
            sp_sex_orientation.requestFocus()
            requiredConditions = false
        }
        return requiredConditions
    }

    val GET_FROM_GALLERY = 3
    val GET_FROM_CAMERA = 4

    var bitmap: Bitmap? = null
    lateinit var imageString: String
    private val sexOrientArray = arrayOf("Cuál es tu orientación sexual?", "Gay", "Lesbiana", "Bisexual", "Transexual", "Pansexual", "Heterosexual", "Otros")
    /*private val etName by bindeasion<EditText>(R.id.et_name)
    private val etDescription by bindeasion<EditText>(R.id.et_description)
    private val btRegister by bindeasion<Button>(R.id.register_button)
    private val imageJoin by bindeasion<CircleImageView>(R.id.imageJoin)
    //private val buttonClick = AlphaAnimation(1f, 0.85f)


    fun <T : View> Activity.bindeasion(@IdRes res: Int): Lazy<T> {
        @Suppress("UNCHECKED_CAST")
        return lazy { findViewById(res) as T }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        register_button.setOnClickListener(this)
        imageJoin.setOnClickListener(this)

        val orienAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, sexOrientArray)
        sp_sex_orientation.setAdapter(orienAdapter)
        sp_sex_orientation.setOnItemSelectedListener(this)

    }

    private fun checkPermisionCamera() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 999)

        } else {
            showPictureDialog()
        }
    }

    private fun showPictureDialog() {

        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Selecciona una opción")
        val pictureDialogItems = arrayOf("Hacer una foto", "Elegir una foto de tu galería")

        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                1 -> choosePhotoFromGallery()
                0 -> takePhotoFromCamera()
            }
        }

        pictureDialog.show()
    }

    fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GET_FROM_GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, GET_FROM_CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                GET_FROM_GALLERY -> if (data != null) {
                    val contentURI = data.data
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI)

                        bitmap?.let {
                            imageString = getStringImage(it)
                        }
                        Toast.makeText(this, "S'ha fet bé from gallery", Toast.LENGTH_LONG).show()
                        //photoRequestMethod()
                        //imageString sería lo que enviarías a Firebase para guardar la foto de usuario en la database

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                    }

                }
                GET_FROM_CAMERA -> {
                    bitmap = data!!.extras!!.get("data") as Bitmap
                    bitmap?.let {
                        imageString = getStringImage(it)
                    }

                    Toast.makeText(this, "S'ha fet bé from camera", Toast.LENGTH_LONG).show()

                    //photoRequestMethod()
                }
            }
        }
    }

    private fun getStringImage(bmp: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

}




