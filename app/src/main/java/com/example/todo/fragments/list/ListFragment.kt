package com.example.todo.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.R
import com.example.todo.data.models.ToDoData
import com.example.todo.data.viewmodel.ToDoViewModel
import com.example.todo.databinding.FragmentListBinding
import com.example.todo.fragments.SharedViewModel
import com.example.todo.fragments.list.adapter.ListAdapter
import com.example.todo.fragments.utils.hideKeyboard
import com.example.todo.fragments.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.text.FieldPosition

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

  private val mToDoViewModel: ToDoViewModel by viewModels()
  private val mSharedViewModel: SharedViewModel by viewModels()

  private var _binding: FragmentListBinding? = null
  private val binding get() = _binding!!

  private val adapter: ListAdapter by lazy { ListAdapter() }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    //val view = inflater.inflate(R.layout.fragment_list, container, false)

    // Data binding
    _binding = FragmentListBinding.inflate(inflater, container, false)
    binding.lifecycleOwner = this
    binding.mSharedViewModel = mSharedViewModel

    //val recyclerView = view.recyclerView
    //recyclerView.adapter = adapter
    //recyclerView.layoutManager = LinearLayoutManager(requireActivity())

    // Setup Recycler View
    setupRecyclerView()

    // Observing LiveData
    mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
      mSharedViewModel.checkIfDatabaseEmpty(data)
      adapter.setData(data)
    })

    //mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner, Observer {
      //showEmptyDatabaseViews(it)
    //})

    //view.floatingActionButton.setOnClickListener {
      //findNavController().navigate(R.id.action_listFragment_to_addFragment)
    //}

    setHasOptionsMenu(true)

    // Hide soft keyboard
    hideKeyboard(requireActivity())

    //return view
    return binding.root
  }

  private fun setupRecyclerView() {
    val recyclerView = binding.recyclerView
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(requireActivity())

    // Swipe to Delete
    swipeToDelete(recyclerView)
  }

  private fun swipeToDelete(recyclerView: RecyclerView) {
    val swipeToDeleteCallback = object : SwipeToDelete() {
      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val deletedItem = adapter.dataList[viewHolder.adapterPosition]
        // Delete Item
        mToDoViewModel.deleteItem(deletedItem)
        adapter.notifyItemRemoved(viewHolder.adapterPosition)
        // Restore Deleted Data
        restoreDeletedData(viewHolder.itemView, deletedItem)
      }
    }
    val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }

  private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
    val snackBar = Snackbar.make(
      view, "Deleted '${deletedItem.title}'",
      Snackbar.LENGTH_LONG
    )
    snackBar.setAction("Undo") {
      mToDoViewModel.insertData(deletedItem)
      //adapter.notifyItemChanged(position)
    }
    snackBar.show()
  }

  //private fun showEmptyDatabaseViews(emptyDatabse: Boolean) {
    //if (emptyDatabase) {
      //view?.no_data_imageView?.visibility = View.VISIBLE
      //view?.no_data_textView?.visibility = View.VISIBLE
    //} else {
      //view?.no_data_imageView?.visibility = View.INVISIBLE
      //view?.no_data_textView?.visibility = View.INVISIBLE
    //}
  //}

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.list_fragment_menu, menu)

    val search = menu.findItem(R.id.menu_search)
    val searchView = search.actionView as? SearchView
    searchView?.isSubmitButtonEnabled = true
    searchView?.setOnQueryTextListener(this)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_delete_all -> confirmRemoval()
      R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(viewLifecycleOwner, Observer {
        adapter.setData(it)
      })
      R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(viewLifecycleOwner, Observer {
        adapter.setData(it)
      })

    }
    return super.onOptionsItemSelected(item)
  }

  override fun onQueryTextSubmit(query: String?): Boolean {
    if (query != null) {
      searchThroughDatabase(query)
    }
    return true
  }

  override fun onQueryTextChange(query: String?): Boolean {
    if (query != null) {
      searchThroughDatabase(query)
    }
    return true
  }

  private fun searchThroughDatabase(query: String) {
    var searchQuery: String = query
    searchQuery = "%$searchQuery%"

    mToDoViewModel.searchDatabase(searchQuery).observeOnce(viewLifecycleOwner, Observer { list ->
      list?.let {
        adapter.setData(it)
      }
    })

  }

  // Show AlertDialog to Confirm Removal of All Items from Database Table
  private fun confirmRemoval() {
    val builder = AlertDialog.Builder(requireContext())
    builder.setPositiveButton("Yes") {_, _ ->
      mToDoViewModel.deleteAll()
      Toast.makeText(requireContext(), "Successfully removed everything!", Toast.LENGTH_SHORT).show()
    }
    builder.setNegativeButton("No") {_, _ -> }
    builder.setTitle("Delete everything?")
    builder.setMessage("Are you sure you want to remove everything'?")
    builder.create().show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

}