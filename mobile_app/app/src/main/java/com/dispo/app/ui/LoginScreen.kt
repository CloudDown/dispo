package com.dispo.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dispo.app.ui.theme.BangersFamily
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DarkBg
import com.dispo.app.ui.theme.DarkBorder
import com.dispo.app.ui.theme.DarkField
import com.dispo.app.ui.theme.DarkSurface
import com.dispo.app.ui.theme.DarkText
import com.dispo.app.ui.theme.DarkTextMuted
import com.dispo.app.ui.theme.DispoGreen
import com.dispo.app.ui.theme.Gold
import com.dispo.app.ui.theme.LedFamily

@Composable
fun LoginScreen(
    isLoading: Boolean,
    error: String?,
    onLogin: (publicId: String, password: String) -> Unit,
    onRegister: (displayName: String, publicId: String, password: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var registerMode by remember { mutableStateOf(false) }
    var publicId by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val submit = {
        if (registerMode) {
            onRegister(displayName, publicId, password)
        } else {
            onLogin(publicId, password)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("DISPO", fontFamily = BangersFamily, fontSize = 52.sp, color = Gold)
        Spacer(Modifier.height(8.dp))
        LedCaption(
            text = if (registerMode) "Crée ton compte" else "Connecte-toi au serveur",
            fontSize = 22.sp,
            color = DarkTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(20.dp))
                .border(2.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (registerMode) {
                AuthField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    placeholder = "Ton pseudo",
                    onDone = submit,
                )
            }
            AuthField(
                value = publicId,
                onValueChange = { publicId = it.removePrefix("@").lowercase() },
                placeholder = if (registerMode) "@pseudo (optionnel)" else "@lea",
                onDone = submit,
            )
            AuthField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Mot de passe",
                password = true,
                onDone = submit,
            )

            error?.let {
                Text(it, color = CircusRed, fontSize = 14.sp)
            }

            Button(
                onClick = submit,
                enabled = !isLoading && password.length >= 4 &&
                    (registerMode || publicId.isNotBlank()) &&
                    (!registerMode || displayName.isNotBlank()),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Cream, modifier = Modifier.height(22.dp))
                } else {
                    Text(
                        if (registerMode) "S'inscrire" else "Se connecter",
                        fontFamily = LedFamily,
                        fontSize = 20.sp,
                        color = Cream,
                    )
                }
            }

            TextButton(onClick = { registerMode = !registerMode }) {
                Text(
                    if (registerMode) "Déjà un compte ? Se connecter" else "Pas de compte ? S'inscrire",
                    color = DarkTextMuted,
                )
            }

            if (!registerMode) {
                Text(
                    "Démo : lea / max / sam — mdp demo",
                    fontFamily = LedFamily,
                    fontSize = 16.sp,
                    color = DarkTextMuted.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    password: Boolean = false,
    onDone: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = DarkTextMuted.copy(alpha = 0.6f)) },
        singleLine = true,
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = DarkBorder,
            focusedContainerColor = DarkField,
            unfocusedContainerColor = DarkField,
            cursorColor = Gold,
            focusedTextColor = DarkText,
            unfocusedTextColor = DarkText,
        ),
    )
}
