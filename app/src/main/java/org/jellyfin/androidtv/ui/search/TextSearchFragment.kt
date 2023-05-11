package org.jellyfin.androidtv.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.leanback.app.RowsSupportFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jellyfin.androidtv.databinding.FragmentSearchTextBinding

class TextSearchFragment : Fragment() {

	private val viewModel by viewModels<SearchViewModel>()

	private var _binding: FragmentSearchTextBinding? = null
	private val binding get() = _binding!!

	private val searchFragmentDelegate = SearchFragmentDelegate(this)

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = FragmentSearchTextBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupSearchBar()
		setupResultFragment()
		observeSearchResults()
	}

	private fun setupSearchBar() {
		binding.searchBar.apply {
			onTextChanged {
				viewModel.searchDebounced(it)
			}
			onSubmit {
				viewModel.search(it)
			}
		}
	}

	private fun setupResultFragment() {
		// Set up result fragment
		val rowsSupportFragment = binding.resultsFrame.getFragment<RowsSupportFragment?>()
		rowsSupportFragment?.let {
			it.adapter = searchFragmentDelegate.rowsAdapter
			it.onItemViewClickedListener = searchFragmentDelegate.onItemViewClickedListener
			it.onItemViewSelectedListener = searchFragmentDelegate.onItemViewSelectedListener
		}
	}

	private fun observeSearchResults() {
		viewModel.searchResultsFlow
			.onEach {
				searchFragmentDelegate.showResults(it)
			}.launchIn(lifecycleScope)
	}

	override fun onDestroyView() {
		_binding = null
		super.onDestroyView()
	}

	private fun EditText.onSubmit(onSubmit: (String) -> Unit) {
		setOnEditorActionListener { _, actionId, _ ->
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				onSubmit(text.toString())
				true
			} else {
				false
			}
		}
	}

	private fun EditText.onTextChanged(onTextChanged: (String) -> Unit) {
		val textWatcher = object : TextWatcher {
			override fun afterTextChanged(s: Editable?) {
				onTextChanged(s.toString())
			}
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
		}
		addTextChangedListener(textWatcher)
	}
}
