package com.jetbrains.kmpapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import org.jetbrains.compose.resources.painterResource
import kmp_app_template.composeapp.generated.resources.Res.drawable
import kmp_app_template.composeapp.generated.resources.Res
import kmp_app_template.composeapp.generated.resources.zerapylogo
import kmp_app_template.composeapp.generated.resources.mlkitlogo
import kmp_app_template.composeapp.generated.resources.zerapylogocombinedcropped
import kmp_app_template.composeapp.generated.resources.Roboto_Medium


@Composable
fun StartScreen(viewModel: PoseDetectionViewModel) {
    val solutions = PoseDetectionSolution.values()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))  // ciemniejszy 141414 jasniejszy 222222
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(Res.drawable.zerapylogocombinedcropped),
            contentDescription = "Combined Zerapy Logo",
            modifier = Modifier
                .size(140.dp)
                .padding(start = 30.dp, top = 16.dp)
        )


        Text(
            text = "Energy Test App",
            fontSize = 45.sp,
            color = Color.White,
            fontFamily = Roboto_medium,
            modifier = Modifier
                .padding(start = 16.dp, top = 30.dp)
        )


        Text(
            text = "for pose detection solutions",
            fontSize = 24.sp,
            color = Color.Gray,
            fontFamily = Roboto_light,
            modifier = Modifier
                .padding(start = 17.dp, top = 10.dp)
        )


        Text(
            text = "Select scanning method",
            fontSize = 30.sp,
            color = Color.Gray, //1A6A73 gray 393939
            fontFamily = Roboto_light,
            modifier = Modifier
                .padding(start = 16.dp, top = 70.dp, bottom = 23.dp)
        )

        // List of solutions
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(solutions) { solution ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.selectSolution(solution) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF222222)), //2E4C55
                        modifier = Modifier
                            .width(270.dp)
                            .height(70.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.mlkitlogo),
                                contentDescription = "MLKit Icon",
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))  // tekst a ikona
                            Text(
                                text = solution.name,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontFamily = Roboto_medium
                            )
                        }
                    }
                }
            }
        }
    }
}
