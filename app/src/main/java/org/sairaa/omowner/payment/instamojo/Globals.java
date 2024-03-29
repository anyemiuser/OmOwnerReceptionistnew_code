package org.sairaa.omowner.payment.instamojo;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.sairaa.omowner.R;
import org.sairaa.omowner.payment.instamojo.model.InstamojoPaymentModel;
import org.sairaa.omowner.payment.instamojo.model.PaymentRequestModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Anuprog on 10/19/2016.
 */

public class Globals {
    public static final String SPF_NAME = "NEXER";

    public static void showToast(Context applicationContext, String msg) {
        try {
            Toast toast = Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG);
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, applicationContext.getResources().getDimension(R.dimen.bottom_text_size));
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static PaymentRequestModel getInstaAmount(
            Context activity,
            PaymentRequestModel paymentRequestModel,
            InstamojoPaymentModel InstamojoPaymentModel,
            String type) {

        paymentRequestModel.setPayment_type("INSTAMOJO");

        try {

            List<org.sairaa.omowner.payment.instamojo.model.InstamojoPaymentModel.InstServiceTaxBean> taxArrayBean = new ArrayList<>();
            taxArrayBean.addAll((List<org.sairaa.omowner.payment.instamojo.model.InstamojoPaymentModel.InstServiceTaxBean>) InstamojoPaymentModel.getInst_service_tax());


            String GST_CARD = "";
            String SERVICE_TAX = "";
            String ANYEMI_CHARGES = paymentRequestModel.getServiceCharge();
            String GATE_WAY_CHARGE = "";
            String TOTAL_AMOUNT_WITH_ALL_CHARGES = "";


            double TERM_BILL_AMOUNT = 0; //with Arrears etc

            //  String FIN_ID = SharedPreferenceUtil.getFIN_ID(activity);
            //String FIN_ID = "53";


            for (int i = 0; i < taxArrayBean.size(); i++) {
                Double upper_limit = Double.parseDouble(taxArrayBean.get(i).getTo());
                Double lower_limit = Double.parseDouble(taxArrayBean.get(i).getFrom());
                TERM_BILL_AMOUNT = Double.parseDouble(paymentRequestModel.getActualDueAmount());


                if (type.equals(taxArrayBean.get(i).getPayment_type()) && upper_limit >
                        TERM_BILL_AMOUNT && lower_limit < TERM_BILL_AMOUNT) {

                    GATE_WAY_CHARGE = taxArrayBean.get(i).getExtra_tax();
                    GST_CARD = taxArrayBean.get(i).getGst_perc();
                    SERVICE_TAX = taxArrayBean.get(i).getExtra_tax_perc();


                    Double card_charges = Double.parseDouble(SERVICE_TAX);
                    Double gst_on_card_charges = Double.parseDouble(GST_CARD);

                    card_charges = (card_charges * TERM_BILL_AMOUNT) / 100;
                    gst_on_card_charges = (card_charges * gst_on_card_charges) / 100;

                    Double final_amount = 0.0;
                    Double bank_charges = 0.0;

                    final_amount = card_charges + TERM_BILL_AMOUNT + gst_on_card_charges;
                    bank_charges = card_charges + gst_on_card_charges + Double.parseDouble(GATE_WAY_CHARGE);

                    TOTAL_AMOUNT_WITH_ALL_CHARGES = String.valueOf(
                            Math.round(final_amount +
                                    Double.parseDouble(GATE_WAY_CHARGE) +
                                    Double.parseDouble(ANYEMI_CHARGES))

                    );

                    paymentRequestModel.setBankCharges(bank_charges + "");
                    paymentRequestModel.setTotal_amount(TOTAL_AMOUNT_WITH_ALL_CHARGES + "");

                    break;
                }
            }


            return paymentRequestModel;


        } catch (Exception e) {
            Globals.showToast(activity, "Invalid Data");
            return null;


        }

    }


    public static PaymentRequestModel getPaymentRequestModes(Bundle parametros) {
        try {
            String data = parametros.getString(Constants.PAYMENT_REQUEST_MODEL);
            PaymentRequestModel paymentRequestModel = new Gson().fromJson(data, PaymentRequestModel.class);
            return paymentRequestModel;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
