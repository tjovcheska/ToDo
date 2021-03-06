package com.example.todo.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.todo.R
import com.example.todo.data.models.ToDoData
import com.example.todo.data.viewmodel.ToDoViewModel
import com.example.todo.databinding.FragmentUpdateBinding
import com.example.todo.fragments.SharedViewModel


class UpdateFragment : Fragment() {

  private val args by navArgs<UpdateFragmentArgs>()

  private val mSharedViewModel: SharedViewModel by viewModels()
  private val mToDoViewModel: ToDoViewModel by viewModels()

  private var _binding: FragmentUpdateBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    // Inflate the layout for this fragment
    //val view = inflater.inflate(R.layout.fragment_update, container, false)

    //Data binding
    _binding = FragmentUpdateBinding.inflate(inflater, container, false)
     binding.args = args

    setHasOptionsMenu(true)

    //view.current_title_et.setText(args.currentItem.title)
    //view.current_description_et.setText(args.currentItem.description)
    //view.current_priorities_spinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
    //view.current_priorities_spinner.onItemSelectedListener = mSharedViewModel.listener

    // Spinner Item Selected Listener
    binding.currentPrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

    //return view
    return binding.root
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.update_fragment_menu, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_save -> updateItem()
      R.id.menu_delete -> confirmItemRemoval()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun updateItem() {
    val title = binding.currentTitleEt.text.toString()
    val description = binding.currentDescriptionEt.text.toString()
    val getPriority = binding.currentPrioritiesSpinner.selectedItem.toString()

    val validation = mSharedViewModel.verifyDataFromUser(title, description)
    if (validation) {
      // Update Current Item
      val updatedItem = ToDoData(
        args.currentItem.id,
        title,
        mSharedViewModel.parsePriority(getPriority),
        description
      )
      mToDoViewModel.updateData(updatedItem)
      Toast.makeText(requireContext(), "Successfully updated!", Toast.LENGTH_SHORT).show()
      // Navigate back
      findNavController().navigate(R.id.action_updateFragment_to_listFragment)
    } else {
      Toast.makeText(requireContext(), "Please fill out all fields!", Toast.LENGTH_SHORT).show()
    }
  }

  // Show alert dialog to confirm Item Removal
  private fun confirmItemRemoval() {
    val builder = AlertDialog.Builder(requireContext())
    builder.setPositiveButton("Yes") {_, _ ->
      mToDoViewModel.deleteItem(args.currentItem)
      Toast.makeText(requireContext(), "Successfully removed: ${args.currentItem.title}", Toast.LENGTH_SHORT).show()
      findNavController().navigate(R.id.action_updateFragment_to_listFragment)
    }
    builder.setNegativeButton("No") {_, _ -> }
    builder.setTitle("Delete '${args.currentItem.title}'?")
    builder.setMessage("Are you sure you want to remove '${args.currentItem.title}'?")
    builder.create().show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}