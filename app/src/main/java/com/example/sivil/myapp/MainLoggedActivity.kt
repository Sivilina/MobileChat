package com.example.sivil.myapp

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.View
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.example.sivil.myapp.fragments.SentFragment
import com.example.sivil.myapp.fragments.TabFragment
import com.example.sivil.myapp.fragments.model.Response
import com.example.sivil.myapp.fragments.model.User
import com.example.sivil.myapp.fragments.network.NetworkUtil
import com.example.sivil.myapp.utils.Constants
import com.google.gson.GsonBuilder
import retrofit2.adapter.rxjava.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException
import java.util.logging.Logger

class MainLoggedActivity : AppCompatActivity() {

    lateinit var mDrawerLayout: DrawerLayout
    lateinit var mNavigationView: NavigationView
    lateinit var mFragmentManager: FragmentManager
    lateinit var mFragmentTransaction: FragmentTransaction
    lateinit var mTvName: TextView
    lateinit var mTvEmail: TextView
    lateinit var mToken: String
    lateinit var mEmail: String

    lateinit var mSubscriptions: CompositeSubscription
    lateinit var mSharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_logged_activity)

        mSubscriptions = CompositeSubscription()

        mDrawerLayout = findViewById<View>(R.id.drawerLayout) as DrawerLayout
        mNavigationView = findViewById<View>(R.id.navView) as NavigationView
        val header = mNavigationView.getHeaderView(0)
        mTvName = header.findViewById(R.id.tv_name) as TextView
        mTvEmail = header.findViewById(R.id.tv_email) as TextView
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mToken = mSharedPreferences.getString(Constants.TOKEN, "")
        mEmail = mSharedPreferences.getString(Constants.EMAIL, "")

        load()
        mFragmentManager = supportFragmentManager
        mFragmentTransaction = mFragmentManager.beginTransaction()
        mFragmentTransaction.replace(R.id.containerView, TabFragment()).commit()

        mNavigationView.setNavigationItemSelectedListener {
            menuItem ->  mDrawerLayout.closeDrawers()

            if (menuItem.itemId == R.id.nav_item_groups){
                val ft = mFragmentManager.beginTransaction()
                ft.replace(R.id.containerView, TabFragment()).commit()
            }
            if (menuItem.itemId == R.id.nav_item_chats) {
                val ft = mFragmentManager.beginTransaction()
                ft.replace(R.id.containerView, SentFragment()).commit()
            }
            false
        }

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        val mDrawerToggle = ActionBarDrawerToggle(this,mDrawerLayout, toolbar,
                R.string.app_name,R.string.app_name)
        mDrawerLayout.setDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()
    }




    fun load(){
        mSubscriptions.add(NetworkUtil.getRetrofit(mToken).getProfile(mEmail).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                    this::handleResponse,this::handleError
                ))
    }


    private fun handleResponse(user: User) {

        mTvName.setText(user.getName())
        mTvEmail.setText(user.getEmail())
       }

    private fun handleError(error: Throwable) {

        if (error is HttpException) {

            val gson = GsonBuilder().create()

            try {

                val errorBody = error.response().errorBody().string()
                val response = gson.fromJson<Any>(errorBody, Response::class.java) as Response
                showSnackBarMessage(response.getMessage())

            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {

            showSnackBarMessage("Network Error !")
        }
    }

    private fun showSnackBarMessage(message: String) {

        Snackbar.make(findViewById(R.id.main_logged_activity), message, Snackbar.LENGTH_SHORT).show()

    }
}
