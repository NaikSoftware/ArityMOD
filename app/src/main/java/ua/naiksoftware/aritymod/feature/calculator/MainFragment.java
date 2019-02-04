// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko
package ua.naiksoftware.aritymod.feature.calculator;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.Observable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import org.javia.arity.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.naiksoftware.aritymod.R;
import ua.naiksoftware.aritymod.databinding.FragmentMainBinding;
import ua.naiksoftware.aritymod.feature.calculator.history.HistoryAdapter;
import ua.naiksoftware.aritymod.feature.graph.FullScreenGraphActivity;
import ua.naiksoftware.aritymod.feature.graph.Graph3dView;
import ua.naiksoftware.aritymod.feature.graph.GraphView;
import ua.naiksoftware.aritymod.service.model.HistoryEntry;

public class MainFragment extends Fragment implements
        View.OnKeyListener,
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        KeyboardView.KeyboardListener {

    private static final char MINUS = '\u2212', TIMES = '\u00d7', DIV = '\u00f7', SQRT = '\u221a', PI = '\u03c0',
            UP_ARROW = '\u21e7', DN_ARROW = '\u21e9';

    private static final KeyEvent KEY_DEL = new KeyEvent(0, KeyEvent.KEYCODE_DEL),
            KEY_ENTER = new KeyEvent(0, KeyEvent.KEYCODE_ENTER);

    private FragmentMainBinding screenBinding;
    private MainViewModel viewModel;

    private TextView result;
    private EditText input;
    private ListView historyView;
    private GraphView graphView;
    private Graph3dView graph3dView;
    private HistoryAdapter adapter;
    private int nDigits = 0;
    private boolean isAlphaVisible;
    private KeyboardView alpha, digits;

    private static final char[][] ALPHA = {
            {'q', 'w', '=', ',', ';', SQRT, '!', '\''},
            {'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
            {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k'},
            {'z', 'x', 'c', 'v', 'b', 'n', 'm', 'l'}
    };

    private static final char[][] DIGITS = {
            {'7', '8', '9', '%', '^', 'A'},
            {'4', '5', '6', '(', ')', 'C'},
            {'1', '2', '3', TIMES, DIV, 'C'},
            {'0', '0', '.', '+', MINUS, 'E'}
    };

    private static final char[][] DIGITS2 = {
            {'0', '.', '+', MINUS, TIMES, DIV, '^', '(', ')', 'C'},
            {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'E'}
    };

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        internalConfigChange(config);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        screenBinding = FragmentMainBinding.inflate(inflater, container, false);
        return screenBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(screenBinding.toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new HistoryAdapter(requireContext(), new ArrayList<>());
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        screenBinding.setViewModel(viewModel);

        internalConfigChange(getResources().getConfiguration());

        viewModel.getHistory().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                List<HistoryEntry> history = viewModel.getHistory().get();
                if (history == null || history.isEmpty()) {
                    String[] init = {
                            "sqrt(pi)\u00f70.5!",
                            "e^(i\u00d7pi)",
                            "ln(e^100)",
                            "sin(x)",
                            "x^2"
                    };
                    for (String s : init) {
                        viewModel.onEnter(s, getResultSpace());
                    }
                }
                if (history != null) {
                    adapter.update(history);
                } else {
                    adapter.update(Collections.emptyList());
                }
            }
        });
    }

    private void internalConfigChange(Configuration config) {

        graphView = screenBinding.graph;
        graph3dView = screenBinding.graph3d;
        historyView = screenBinding.history;

        final boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
//         final boolean hasKeyboard = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;

        alpha = screenBinding.keyboard.alpha;
        digits = screenBinding.keyboard.digits;
        if (isLandscape) {
            digits.init(DIGITS2, false, true, this);
            isAlphaVisible = false;
        } else {
            alpha.init(ALPHA, false, false, this);
            digits.init(DIGITS, true, true, this);
            updateAlpha();
        }

        result = screenBinding.result;

        input = screenBinding.input;
        input.setOnKeyListener(this);
        input.setEditableFactory(new CalculatorEditable.Factory());
        input.setTextIsSelectable(true);
        input.requestFocus();
        InputMethodManager inputManager
                = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                requireActivity().getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_HIDDEN);
        graphView.setOnClickListener(this);
        graph3dView.setOnClickListener(this);
        if (historyView != null) {
            historyView.setAdapter(adapter);
            historyView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        graph3dView.onPause();
        viewModel.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        graph3dView.onResume();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.list_defs: {
                NavHostFragment.findNavController(this).navigate(MainFragmentDirections.openUserDefinitions());
                break;
            }

            case R.id.help:
                NavHostFragment.findNavController(this).navigate(MainFragmentDirections.openHelp());
                break;

            case R.id.clear_history:
                viewModel.clearHistory();
                break;

            case R.id.clear_defs:
                viewModel.clearUserDefinitions();
                break;

            case R.id.settings:
                NavHostFragment.findNavController(this).navigate(MainFragmentDirections.openSettings());
                break;

            case android.R.id.home:
                NavHostFragment.findNavController(this).popBackStack();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View target) {
        if (target == graphView || target == graph3dView) {
            startActivity(new Intent(requireContext(), FullScreenGraphActivity.class));
        }
    }

    @Override
    public void onItemClick(AdapterView parent, View view, int pos, long id) {
        viewModel.moveToHistory(pos);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    doEnter();
                    break;

                case KeyEvent.KEYCODE_DPAD_UP:
                    onUp();
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    onDown();
                    break;
                default:
                    return false;
            }
            return true;
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    return true;
            }
            return false;
        }
    }

    private int getResultSpace() {
        if (nDigits == 0) {
            int width = result.getWidth() - result.getTotalPaddingLeft() - result.getTotalPaddingRight();
            float oneDigitWidth = result.getPaint().measureText("5555555555") / 10f;
            nDigits = (int) (width / oneDigitWidth);
        }
        return nDigits;
    }

    private void updateAlpha() {
        alpha.setVisibility(isAlphaVisible ? View.VISIBLE : View.GONE);
        digits.setAboveView(isAlphaVisible ? alpha : null);
    }

    private StringBuilder oneChar = new StringBuilder(" ");

    @Override
    public void onKeyboardClicked(char key) {
        if (key == 'E') {
            doEnter();
        } else if (key == 'C') {
            doBackspace();
        } else if (key == 'A') {
            isAlphaVisible = !isAlphaVisible;
            updateAlpha();
        } else {
            int cursor = input.getSelectionStart();
            oneChar.setCharAt(0, key);
            input.getText().insert(cursor, oneChar);
        }
    }

    private void showGraph(Function f) {
        if (f == null) {
            if (historyView.getVisibility() != View.VISIBLE) {
                graphView.setVisibility(View.GONE);
                graph3dView.setVisibility(View.GONE);
                graph3dView.onPause();
                historyView.setVisibility(View.VISIBLE);
                result.setVisibility(View.VISIBLE);
            }
        } else {
            if (f.arity() == 1) {
                graphView.setFunction(f);
                if (graphView.getVisibility() != View.VISIBLE) {
                    if (isAlphaVisible) {
                        isAlphaVisible = false;
                        updateAlpha();
                    }
                    result.setVisibility(View.GONE);
                    historyView.setVisibility(View.GONE);
                    graph3dView.setVisibility(View.GONE);
                    graph3dView.onPause();
                    graphView.setVisibility(View.VISIBLE);
                }
            } else {
                graph3dView.setFunction(f);
                if (graph3dView.getVisibility() != View.VISIBLE) {
                    if (isAlphaVisible) {
                        isAlphaVisible = false;
                        updateAlpha();
                    }
                    result.setVisibility(View.GONE);
                    historyView.setVisibility(View.GONE);
                    graphView.setVisibility(View.GONE);
                    graph3dView.setVisibility(View.VISIBLE);
                    graph3dView.onResume();
                }
            }
        }
    }

    private void doEnter() {
        viewModel.onEnter(getResultSpace());
    }

    private void onUp() {
        viewModel
    }

    private void onDown() {
        if (historyService.moveDown(input.getText().toString())) {
            changeInput(historyService.getText());
        }
    }

    private void doBackspace() {
        input.dispatchKeyEvent(KEY_DEL);
    }
}
