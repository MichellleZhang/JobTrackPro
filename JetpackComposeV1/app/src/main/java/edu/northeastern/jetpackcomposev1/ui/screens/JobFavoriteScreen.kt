package edu.northeastern.jetpackcomposev1.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.northeastern.jetpackcomposev1.models.job.JobModel
import edu.northeastern.jetpackcomposev1.viewmodels.ApplicationViewModel
import edu.northeastern.jetpackcomposev1.viewmodels.JobViewModel

@Composable
fun JobFavoriteScreen(
    jobViewModel: JobViewModel,
    applicationViewModel: ApplicationViewModel,
    modifier: Modifier = Modifier,
    onNavigateToJobDetail: () -> Unit,
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        item {
            JobLists(
                jobs = jobViewModel.response.results,
                jobViewedHistoryList = jobViewModel.jobViewedHistoryList,
                jobApplicationList = applicationViewModel.jobApplicationList,
                onSetJobViewedHistory = { jobId -> jobViewModel.setJobViewedHistoryToDB(jobId) },
                onFindJobInFavorite = { jobId -> jobViewModel.findJobInFavoriteList(jobId) },
                onSetJobFavorite = { job -> jobViewModel.setJobFavoriteToDB(job)},
                jobViewModel = jobViewModel,
                onNavigateToJobDetail = onNavigateToJobDetail
            )
        }
    }
}