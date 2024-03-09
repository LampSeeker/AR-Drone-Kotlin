package com.portfolio.ardronekotlin

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

import com.portfolio.ardronekotlin.databinding.ActivityLoginBinding
import com.portfolio.ardronekotlin.databinding.DialogForgotpasswordBinding


class LoginActivity : AppCompatActivity() {

    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var auth : FirebaseAuth
    lateinit var imageview : ImageView

    val binding by lazy{ActivityLoginBinding.inflate(layoutInflater)}
    var googleLoginReult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        var data = result.data
        var task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account.idToken)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        //클릭 기능 연결
        binding.btnSignInGoogle.setOnClickListener{
            signInGoogle()
        }
        binding.btnSignInEmail.setOnClickListener {
            signInAndSignUp()
        }
        binding.txtForgotPassword.setOnClickListener {
            val userEmail = binding.edtEmail
            //다이얼로그용 바인딩
            val dialogBinding = DialogForgotpasswordBinding.inflate(layoutInflater)
            val builder = AlertDialog.Builder(this)
            builder.setView(dialogBinding.root)
            val dialog = builder.create()
            dialogBinding.btnReset.setOnClickListener {
            //pending
                compareEmail(userEmail)
                dialog.dismiss()
            }
            dialogBinding.btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            if(dialog.window != null){
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    //구글 로그인
    private fun firebaseAuthWithGoogle(idToken: String?){
        var credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential((credential)).addOnCompleteListener{
                task ->
            if(task.isSuccessful){
                moveMainPage(task.result?.user)
            }
        }
    }

    private fun signInGoogle() {
        val i = googleSignInClient.signInIntent
        googleLoginReult.launch(i)
    }

    //이메일 로그인
    private fun signInAndSignUp(){
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()
        // 입력 유효성 검사
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일 주소를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }
        auth?.createUserWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
            task ->
            if(task.isSuccessful) {
                Toast.makeText(this,"가입 성공",Toast.LENGTH_SHORT).show()
                moveMainPage(task.result.user)
                }
            else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            else {
                signInEmail()
                }
            }
        }

    private fun signInEmail(){
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()
        auth?.signInWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
                task ->
            if(task.isSuccessful) {
                Toast.makeText(this,"인증 성공",Toast.LENGTH_SHORT).show()
                moveMainPage(task.result.user)
            }
            else {
                Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    //ForgotPassword 처리
    private fun compareEmail(email:EditText){
        if(email.text.toString().isEmpty()){
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher((email.text.toString())).matches()){
            return
        }
        auth.sendPasswordResetEmail(email.text.toString())
            .addOnCompleteListener {
                task->
                if(task.isSuccessful){
                    Toast.makeText(this,"재설정 메일을 보냈습니다.",Toast.LENGTH_SHORT).show()
                }
            }
    }

    //mainactivity로 이동
    private fun moveMainPage(user: FirebaseUser?){
        if (user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

}