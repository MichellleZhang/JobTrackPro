package edu.northeastern.jetpackcomposev1.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.northeastern.jetpackcomposev1.R
import edu.northeastern.jetpackcomposev1.models.JobViewModel
import edu.northeastern.jetpackcomposev1.ui.theme.JetpackComposeV1Theme
import edu.northeastern.jetpackcomposev1.utility.checkIfNew
import edu.northeastern.jetpackcomposev1.utility.convertDateTime
import edu.northeastern.jetpackcomposev1.utility.convertNumberOfJobs
import edu.northeastern.jetpackcomposev1.utility.convertSalary

@Composable
fun JobSearchScreen(jobViewModel: JobViewModel, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        item { SearchSection(jobViewModel) }
        item { JobLists(jobViewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSection(jobViewModel: JobViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
        OutlinedTextField(
            value = "Not a real search bar",
            onValueChange = {  },
            label = { Text("Search") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        OutlinedTextField(
            value = "Not a real search bar",
            onValueChange = {  },
            label = { Text("Location") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        LazyRow(verticalAlignment = Alignment.CenterVertically) {
            item { IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_filter_list_24),
                    contentDescription = "Filter",
                    tint = MaterialTheme.colorScheme.outline
                )
            } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
            item { Button(onClick = { /*TODO*/ }) { Text("Other Button") } }
        }
        Text(
            text = convertNumberOfJobs(jobViewModel.response.count),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelSmall,
            modifier = modifier.padding(start = 16.dp)
        )
    }
}

//// we can preview individual UI also
@Preview(showBackground = true)
@Composable
fun PreviewJobSearchScreen(){
    JetpackComposeV1Theme {
        JobSearchScreen(viewModel())
    }
}

@Composable
fun JobLists(jobViewModel: JobViewModel, modifier: Modifier = Modifier) {
//    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        jobViewModel.response.results.forEach { item ->
            JobCard(
                title = item.title,
                company = item.company.display_name,
                location = item.location.display_name,
                contract_time = item.contract_time,
                salary_min = item.salary_min,
                salary_max = item.salary_max,
                time = item.created
            )
        }
//    }
}

@Composable
fun JobCard(
    title: String,
    company: String,
    location: String,
    contract_time: String,
    salary_min: Double,
    salary_max: Double,
    time: String,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            CardHead(time = time)
            JobContent(
                title = title,
                company = company,
                location = location,
                contract_time = contract_time,
                salary_min = salary_min,
                salary_max = salary_max,
                time = time
            )
            CardFoot()
        }
    }
}

@Composable
fun JobContent(
    title: String,
    company: String,
    location: String,
    contract_time: String,
    salary_min: Double,
    salary_max: Double,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .padding(start = 8.dp)
        .clickable { /*TODO*/ }
    ) {
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = company,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = location,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = contract_time.replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(all = 4.dp)
                )
            }
            Surface(
                modifier = Modifier.padding(start = 4.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = convertSalary(salary_min, salary_max),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(all = 4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = convertDateTime(time),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun CardHead(time: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.weight(0.5f)) {
            if(checkIfNew(time)) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_fiber_new_24),
                    contentDescription = "New",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Row(modifier = Modifier.weight(0.5f), horizontalArrangement = Arrangement.End) {
            if(!checkIfNew(time)) { /*TODO*/
                Text(
                    text = "Applied",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun CardFoot() {
    val outlineIcon = R.drawable.outline_bookmark_border_24
    val fillIcon = R.drawable.baseline_bookmark_24
    var id by rememberSaveable { mutableIntStateOf(outlineIcon) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { id = if (id == outlineIcon) fillIcon else outlineIcon }) { /*TODO*/
            Icon(
                painter = painterResource(id = id),
                contentDescription = "Saved",
                tint = MaterialTheme.colorScheme.outline
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_share_24),
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}