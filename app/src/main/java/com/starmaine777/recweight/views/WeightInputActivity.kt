package com.starmaine777.recweight.views

import android.nfc.Tag
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.starmaine777.recweight.R
import com.starmaine777.recweight.data.WeightItemDao
import com.starmaine777.recweight.data.WeightItemEntity
import com.starmaine777.recweight.data.WeightItemsViewModel
import com.starmaine777.recweight.utils.Consts
import io.reactivex.FlowableSubscriber
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscriber

class WeightInputActivity : AppCompatActivity() {

    companion object {
        const val ARGS_INPUT_TYPE = "inputType"
    }

    val weightInfoVm: WeightItemsViewModel by lazy { WeightItemsViewModel(application) }
    val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight_input)

        val inputFragment: Fragment = WeightInputFragment.newInstance(intent.getSerializableExtra(ARGS_INPUT_TYPE) as Consts.WEIGHT_INPUT_MODE)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, inputFragment, WeightInputFragment.TAG)
                .commit()

    }

    override fun onResume() {
        super.onResume()
        disposable.add(weightInfoVm.getWeightItemList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    t: WeightItemEntity ->  Log.d("test", "weightItem:id=${t.id}, date=${t.recTime}, weight = ${t.weight}, fat = ${t.fat}")
                }
                ))
        Log.d("test", "end onResume")

    }
}
