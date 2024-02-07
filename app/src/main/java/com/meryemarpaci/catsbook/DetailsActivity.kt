package com.meryemarpaci.catsbook


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.meryemarpaci.catsbook.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncer: ActivityResultLauncher<String>
    private var selectedBitmap: Bitmap? = null
    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Cats", Context.MODE_PRIVATE, null)

        registerLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")

        if(info.equals("old")) {
            binding.button.visibility=View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)
            val cursor =
                database.rawQuery("SELECT * FROM cats WHERE id = ?", arrayOf(selectedId.toString()))

            val catNameIx = cursor.getColumnIndex("catname")
            val catWhenIx = cursor.getColumnIndex("catwhen")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.catName.setText(cursor.getString(catNameIx))
                binding.catWhen.setText(cursor.getString(catWhenIx))
                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }




    }

    fun saveBtnClick(view: View) {
        //SQLite yerel veri tabanı, hafızada yer tutar
        //max 1 mblik row
        val catName = binding.catName.text.toString()
        val catWhen = binding.catWhen.text.toString()

        if (selectedBitmap != null) {
            // !! null kontrolü için
            val smallBitmap = makeSmallBitmap(selectedBitmap!!, 300)
            //byte dizisi, görseli veriye çevirme, veritabanı kaydı için
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS cats (id INTEGER PRIMARY KEY, catname VARCHAR, catwhen VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO cats (catname,catwhen,image) VALUES (?,?,?)"

                //sql stringi ister
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, catName) //index 1den başlar
                statement.bindString(2, catWhen)
                statement.bindBlob(3, byteArray)

                statement.execute()//uygulama, bağlama

            } catch (e: Exception) {
                e.printStackTrace()

            }

            val intent = Intent(this, MainActivity::class.java)
            //bundan once açık olan aktivitelerin epsini kapat demek
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)


        }

    }

    //bitmap küçülten fonksiyon
    private fun makeSmallBitmap(image: Bitmap, maxSize: Int): Bitmap {

        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            //yatay, lanscape
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()


        } else {
            //dikey, portrait
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()

        }

        return Bitmap.createScaledBitmap(image, width, height, true)

    }

    //tıklayıp cihazdan resim yukleme
    fun selectImg(view: View) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //android 33+ için READ_MEDİA_IMAGES
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //Tekrar izin iste
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    //rationale
                    //LENGTH_INDEFINITE belirsiz süre, kullanıcı okeye basana kadar
                    Snackbar.make(view, "Gallery permission needed", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission") {
                            //izin iste
                            permissionLauncer.launch(Manifest.permission.READ_MEDIA_IMAGES)


                        }.show()
                } else {
                    //göstermeden iste
                    permissionLauncer.launch(Manifest.permission.READ_MEDIA_IMAGES)

                }

            } else {
                //Galeriden bir intentin urisini al
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }

        } else {
            //Android 32- -> READ_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //Tekrar izin iste
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //rationale
                    //LENGTH_INDEFINITE belirsiz süre, kullanıcı okeye basana kadar
                    Snackbar.make(view, "Gallery permission needed", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission", View.OnClickListener {
                            //izin iste
                            permissionLauncer.launch(Manifest.permission.READ_EXTERNAL_STORAGE)


                        }).show()
                } else {
                    //göstermeden iste
                    permissionLauncer.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }

            } else {
                //Galeriden bir intentin urisini al
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)


            }
        }


    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        //alinan uri bitmape cevrilmeli
                        //binding.imageView.setImageURI(imageData)
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {

                                    val source = ImageDecoder.createSource(
                                        this@DetailsActivity.contentResolver,
                                        imageData
                                    )
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)

                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                                        contentResolver,
                                        imageData
                                    )
                                    binding.imageView.setImageBitmap(selectedBitmap)


                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                }
            }
        permissionLauncer =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                    //permission granted
                } else {
                    //permission denied
                    Toast.makeText(this@DetailsActivity, "Permission needed!", Toast.LENGTH_LONG)
                        .show()
                }
            }

    }

}