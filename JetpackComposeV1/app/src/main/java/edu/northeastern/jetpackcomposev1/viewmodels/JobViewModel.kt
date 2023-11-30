package edu.northeastern.jetpackcomposev1.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import edu.northeastern.jetpackcomposev1.models.job.JobFavoriteModel
import edu.northeastern.jetpackcomposev1.models.job.JobModel
import edu.northeastern.jetpackcomposev1.models.job.JobSearchHistoryModel
import edu.northeastern.jetpackcomposev1.models.job.JobSearchResultModel
import edu.northeastern.jetpackcomposev1.models.job.JobViewedHistoryModel
import edu.northeastern.jetpackcomposev1.models.search.SearchModel
import edu.northeastern.jetpackcomposev1.utility.urlEncoding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class JobViewModel: ViewModel() {
    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database
    val storage: FirebaseStorage = Firebase.storage
    var running: Boolean by mutableStateOf(false)
    var search: SearchModel by mutableStateOf(SearchModel())
    var response: JobSearchResultModel by mutableStateOf(JobSearchResultModel())
    var jobSearchHistoryList: SnapshotStateList<JobSearchHistoryModel> = mutableStateListOf()
    var jobViewedHistoryList: SnapshotStateList<JobViewedHistoryModel> = mutableStateListOf()
    var jobFavoriteList: SnapshotStateList<JobFavoriteModel> = mutableStateListOf()

    //Jun's modification
    //adding a new state to keep track of the current job
    //the private val
    private val _selectedJob = mutableStateOf<JobModel?>(null)
    // expose the val to the outside, this is the getter
    // usage: jobViewModel.selectedJob.value
    val selectedJob: State<JobModel?>
        get() = _selectedJob
    //this function is for setting the selected job
    fun selectJob(job: JobModel) {
        _selectedJob.value = job
    }


    /**********************************************************************************************/
    fun setRequestURL(): String {
        val requestHead = "https://api.adzuna.com/v1/api/jobs"
        val app_id = "?app_id=${search.app_id}"
        val app_key = "&app_key=${search.app_key}"
        val results_per_page = "&results_per_page=${search.results_per_page}"
        var requestURL = "${requestHead}/${search.country}/search/${search.page}" + app_id + app_key + results_per_page
        if (search.what.isNotEmpty()) { requestURL += "&what=${urlEncoding(search.what)}" }
        if (search.what_and.isNotEmpty()) { requestURL += "&what_end=${search.what_and}" }
        if (search.what_phrase.isNotEmpty()) { requestURL += "&what_phrase=${search.what_phrase}" }
        if (search.what_or.isNotEmpty()) { requestURL += "&what_or=${search.what_or}" }
        if (search.what_exclude.isNotEmpty()) { requestURL += "&what_exclude=${search.what_exclude}" }
        if (search.title_only.isNotEmpty()) { requestURL += "&title_only=${search.title_only}" }
        if (search.where.isNotEmpty()) { requestURL += "&where=${urlEncoding(search.where)}&distance=${search.distance}" }

        return requestURL
    }

    fun getJobFromAPI() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                running = true
                val client = HttpClient(Android) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
                // Make the HTTP request.
                response = client.get(setRequestURL()).body()
                client.close()
                running = false
            }
        }
    }
    /**********************************************************************************************/
    fun getJobSearchHistoryFromDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                val myRef = database.getReference("users/${auth.currentUser?.uid}/jobSearchHistory")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val jobSearchHistoryModel = snapshot.getValue(JobSearchHistoryModel::class.java)
                            if (jobSearchHistoryModel != null) {
                                // there is a Recomposition issue, i use this approach to fix the problem, later we can find a better solution
                                val index = jobSearchHistoryList.indexOfFirst { it.id == jobSearchHistoryModel.id }
                                if (index == -1) {
                                    jobSearchHistoryList.add(jobSearchHistoryModel)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("debug", "Failed to read job search history from DB.", error.toException())
                    }
                })
            }
        }
    }
    fun setJobSearchHistoryToDB(isInsert: Boolean, deleteIndex: Int = 0) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                if (isInsert) {
                    val newSearch = JobSearchHistoryModel(country = search.country, what = search.what, where = search.where, distance = search.distance)
                    val index = jobSearchHistoryList.indexOfFirst { it.country == newSearch.country && it.what == newSearch.what && it.where == newSearch.where && it.distance == newSearch.distance }
                    if (index != -1) {
                        jobSearchHistoryList.removeAt(index)
                    }
                    jobSearchHistoryList.add(0, newSearch)
                }
                else {
                    jobSearchHistoryList.removeAt(deleteIndex)
                }
                database.getReference("users/${auth.currentUser?.uid}/jobSearchHistory").setValue(jobSearchHistoryList.toList())
            }
        }
    }
    /**********************************************************************************************/
    fun getJobViewedHistoryFromDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                val myRef = database.getReference("users/${auth.currentUser?.uid}/jobViewedHistory")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val jobViewedHistoryModel = snapshot.getValue(JobViewedHistoryModel::class.java)
                            if (jobViewedHistoryModel != null) {
                                // there is a Recomposition issue, i use this approach to fix the problem, later we can find a better solution
                                val index = jobViewedHistoryList.indexOfFirst { it.id == jobViewedHistoryModel.id }
                                if (index == -1) {
                                    jobViewedHistoryList.add(jobViewedHistoryModel)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("debug", "Failed to read job viewed history from DB.", error.toException())
                    }
                })
            }
        }
    }
    fun setJobViewedHistoryToDB(jobId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                val jobViewHistory = JobViewedHistoryModel(jobId)
                val index = jobViewedHistoryList.indexOfFirst { it.id == jobId }
                if (index != -1) {
                    jobViewedHistoryList.removeAt(index)
                }
                jobViewedHistoryList.add(0, jobViewHistory)
                database.getReference("users/${auth.currentUser?.uid}/jobViewedHistory").setValue(jobViewedHistoryList.toList())
            }
        }
    }
    /**********************************************************************************************/
    fun getJobFavoriteFromDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                val myRef = database.getReference("users/${auth.currentUser?.uid}/jobFavorites")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val jobFavoriteModel = snapshot.getValue(JobFavoriteModel::class.java)
                            if (jobFavoriteModel != null) {
                                // there is a Recomposition issue, i use this approach to fix the problem, later we can find a better solution
                                val index = jobFavoriteList.indexOfFirst { it.id == jobFavoriteModel.id }
                                if (index == -1) {
                                    jobFavoriteList.add(jobFavoriteModel)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("debug", "Failed to read favorites from DB.", error.toException())
                    }
                })
            }
        }
    }
    fun findJobInFavoriteList(jobId: String): Boolean {
        return jobFavoriteList.any { it.id == jobId }
    }
    fun setJobFavoriteToDB(job: JobModel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Your long-running operation here
                if (findJobInFavoriteList(job.id)) {
                    jobFavoriteList.removeIf {it.id == job.id}
                } else {
                    val favoriteJob = JobFavoriteModel(id = job.id, job = job.copy())
                    jobFavoriteList.add(0, favoriteJob)
                }
                database.getReference("users/${auth.currentUser?.uid}/jobFavorites").setValue(jobFavoriteList.toList())
            }
        }
    }
    /**********************************************************************************************/



}