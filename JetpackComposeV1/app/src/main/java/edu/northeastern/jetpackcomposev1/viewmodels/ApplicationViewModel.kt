package edu.northeastern.jetpackcomposev1.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import edu.northeastern.jetpackcomposev1.models.application.Event
import edu.northeastern.jetpackcomposev1.models.application.TimeLine
import edu.northeastern.jetpackcomposev1.models.job.JobApplicationModel
import edu.northeastern.jetpackcomposev1.models.job.JobModel
import edu.northeastern.jetpackcomposev1.models.resume.ResumeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale.filter

class ApplicationViewModel: ViewModel() {
    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database
    val storage: FirebaseStorage = Firebase.storage

    var jobApplicationList: SnapshotStateList<JobApplicationModel> = mutableStateListOf()

    //Jun's modification
    //adding state for recording the selected application and selected event
    private val _selectedApplication = mutableStateOf<JobApplicationModel?>(null)
    private val _selectedEvent = mutableStateOf<Event?>(null)

    //getter
    val selectedApplication: State<JobApplicationModel?> get() = _selectedApplication

    val selectedEvent: State<Event?> get() = _selectedEvent

    // setter
    fun selectEvent(event: Event) {
        _selectedEvent.value = event
    }

    fun selectApplication(application: JobApplicationModel) {
        _selectedApplication.value = application
    }


    /**********************************************************************************************/


    fun getJobApplicationFromDB() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val myRef = database.getReference("users/${auth.currentUser?.uid}/jobApplications")
                myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            val jobApplicationModel =
                                snapshot.getValue(JobApplicationModel::class.java)
                            if (jobApplicationModel != null) {
                                val index =
                                    jobApplicationList.indexOfFirst { it.id == jobApplicationModel.id }
                                if (index == -1) {
                                    jobApplicationList.add(jobApplicationModel)
                                }

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Failed to read value
                        Log.w("debug", "Failed to read applications from DB.", error.toException())
                    }
                })
            }
        }
    }

    fun setJobApplicationToDB(job: JobModel, resume: ResumeModel, timeLine: TimeLine) {
        val newJobApplication = JobApplicationModel(job = job.copy())
        newJobApplication.resume = resume
        newJobApplication.timeLine = timeLine.copy()
        newJobApplication.status = timeLine.results.first().status
        jobApplicationList.add(newJobApplication)
        selectApplication(newJobApplication)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.getReference("users/${auth.currentUser?.uid}/jobApplications")
                    .setValue(jobApplicationList.toList())
            }
        }
    }

    private fun updateJobApplicationToDB(
        oldJobApplication: JobApplicationModel,
        newJobApplication: JobApplicationModel
    ) {
        jobApplicationList.remove(oldJobApplication)
        jobApplicationList.add(newJobApplication)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.getReference("users/${auth.currentUser?.uid}/jobApplications")
                    .setValue(jobApplicationList.toList())
            }

        }
    }

    fun updateEventToDB(jobApplication: JobApplicationModel, oldEvent: Event, newEvent: Event) {
        val newJobApplication = JobApplicationModel(job = jobApplication.job.copy())
        newJobApplication.resume = jobApplication.resume
        newJobApplication.timeLine = jobApplication.timeLine.copy()
        val mutableResults = newJobApplication.timeLine.results.toMutableList()
        if (newEvent.date.isNotBlank() && newEvent.status.isNotBlank()) {
            mutableResults.add(newEvent)
        }
        if (oldEvent.date.isNotBlank() && oldEvent.status.isNotBlank()) {
            mutableResults.removeIf { it.date == oldEvent.date && it.status == oldEvent.status }
        }
        val updatedTimeLine = newJobApplication.timeLine.copy(
            results = mutableResults.sortedByDescending { it.date },
            count = mutableResults.size
        )
        newJobApplication.timeLine = updatedTimeLine
        newJobApplication.status = updatedTimeLine.results.first().status
        selectApplication(newJobApplication)
        updateJobApplicationToDB(jobApplication, newJobApplication)
    }

}