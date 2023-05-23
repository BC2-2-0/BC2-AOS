import android.app.Dialog
import android.content.Context
import android.view.WindowManager
import com.example.gsm_bc2_android.R

class CustomDialog(context: Context)
{
    private val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener

    fun setOnClickListener(listener: OnDialogClickListener)
    {
        onClickListener = listener
    }


    fun showDialog()
    {
        dialog.setContentView(R.layout.pay_dialog)
        dialog.window!!.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.show()

    }

    interface OnDialogClickListener
    {
        fun onClicked(name: String)
    }

}