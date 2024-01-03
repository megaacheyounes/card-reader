@file:OptIn(ExperimentalMaterial3Api::class)

package com.younes.generic.cardreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.devnied.emvnfccard.parser.EmvTemplate
import com.younes.generic.cardreader.ui.RippleLoadingAnimation
import com.younes.generic.hospaydemomobile.ui.theme.HosPayDemoMobileTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "MyLog"
fun log(msg: String) = Log.d(TAG, msg)

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {


    private lateinit var nfcAdapter: NfcAdapter

    private var cardDetailsState by mutableStateOf<CardDetails?>(null)

    var config: EmvTemplate.Config = EmvTemplate.Config()
        .setContactLess(true) // Enable contact less reading (default: true)
        .setReadAllAids(true) // Read all aids in card (default: true)
        .setReadTransactions(false) // Read all transactions
        .setRemoveDefaultParsers(false) // Remove default parsers for GeldKarte and EmvCard (default: false)
        .setReadAt(false) // Read and extract ATR/ATS and description


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            HosPayDemoMobileTheme {
                MainContent(cardDetailsState)
            }
        }
    }


    public override fun onResume() {
        super.onResume()
        nfcAdapter.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        log("onTagDiscovered")
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        // Create Parser
        val parser = EmvTemplate.Builder()
            .setProvider(IsoDepProvider(isoDep)) // Define provider
            .setConfig(config) // Define config
            .build()

        // Read card
        val emvCard = parser.readEmvCard()

        isoDep.close()

        log("card detected $emvCard")

        runOnUiThread {
            cardDetailsState = CardDetails(
                emvCard.cardNumber ?: "0".repeat(16),
                (emvCard.expireDate ?: Date(0)).formatAsMonthYear()
            )
        }

    }


}

@Preview
@Composable
fun MainContent(
    cardDetails: CardDetails? = CardDetails("1234 1234 1234 1234", "12/28")
) {
    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "POS machine emulator (Card reader)",
                style = MaterialTheme.typography.titleMedium
            )

            TapCard()

            cardDetails?.let {
                Spacer(Modifier.height(32.dp))
                CardDetails(cardDetails)
            }
        }
    }
}

@Composable
fun CardDetails(
    cardDetails: CardDetails
) {
    val formattedNumber = remember {
        cardDetails.number.split("").toMutableList().apply {
            add(5, " ")
            add(5 * 2, " ")
            add(5 * 3, " ")
        }.joinToString("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)

    ) {

        val fontStyle = MaterialTheme.typography.bodyLarge.copy(
            letterSpacing = 4.sp,
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif
        )

        Icon(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp, end = 8.dp)
                .size(64.dp),
            painter = painterResource(id = R.drawable.nfc),
            tint = Color.White,
            contentDescription = null
        )


        Image(
            modifier = Modifier
                .padding(start = 32.dp)
                .height(64.dp),
            painter = painterResource(id = R.drawable.chip),
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp),
            text = formattedNumber,
            color = Color.White,
            style = fontStyle
        )

        Row(
            Modifier
                .padding(top = 8.dp, start = 64.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VALID\nTHRU",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp
                ),
                color = Color.White
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = cardDetails.expiryDate,
                color = Color.White,
                style = fontStyle
            )
        }
    }

}


@Composable
fun TapCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {
            RippleLoadingAnimation(
                modifier = Modifier
                    .size(size = 200.dp)
            )

            Icon(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Cyan),
                tint = Color.White,
                painter = painterResource(R.drawable.tap_to_pay),
                contentDescription = null
            )
        }

        Text(
            text = "Tap Card",
            style = MaterialTheme.typography.titleLarge
        )

    }
}


fun Date.formatAsMonthYear(): String = SimpleDateFormat("MM/yy", Locale.US).format(this)





