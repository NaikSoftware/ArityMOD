// Copyright (C) 2009 Mihai Preda
package ua.naiksoftware.aritymod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.javia.arity.Complex;
import org.javia.arity.Function;
import org.javia.arity.FunctionAndName;
import org.javia.arity.Symbols;
import org.javia.arity.SyntaxException;
import org.javia.arity.Util;

import java.util.ArrayList;

import ua.naiksoftware.utils.ParcelableBinder;

public class MainActivity extends AppCompatActivity implements TextWatcher,
        View.OnKeyListener,
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        KeyboardView.KeyboardListener,
        Toolbar.OnMenuItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    static final char MINUS = '\u2212', TIMES = '\u00d7', DIV = '\u00f7', SQRT = '\u221a', PI = '\u03c0',
            UP_ARROW = '\u21e7', DN_ARROW = '\u21e9';

    private static final int MSG_INPUT_CHANGED = 1;
    private static final String INFINITY = "Infinity";
    private static final String INFINITY_UNICODE = "\u221e";

    static Symbols symbols = new Symbols();
    static Function function;

    private TextView result;
    private EditText input;
    private ListView historyView;
    private GraphView graphView;
    private Graph3dView graph3dView;
    private History history;
    private HistoryAdapter adapter;
    private int nDigits = 0;
    private boolean pendingClearResult;
    private boolean isAlphaVisible;
    private KeyboardView alpha, digits;
    static ArrayList<Function> graphedFunction;
    static Defs defs;
    private ArrayList<Function> auxFuncs = new ArrayList<>();
    static boolean useHighQuality3d = true;

    private static final char[][] ALPHA = {
            {'q', 'w', '=', ',', ';', SQRT, '!', '\''},
            {'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
            {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k'},
            {'z', 'x', 'c', 'v', 'b', 'n', 'm', 'l'},};

    private static final char[][] DIGITS = {
            {'7', '8', '9', '%', '^', 'A'},
            {'4', '5', '6', '(', ')', 'C'},
            {'1', '2', '3', TIMES, DIV, 'C'},
            {'0', '0', '.', '+', MINUS, 'E'},};

    private static final char[][] DIGITS2 = {
            {'0', '.', '+', MINUS, TIMES, DIV, '^', '(', ')', 'C'},
            {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'E'},};

    /*
     private static final char[][] DIGITS3 = {
     {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
     {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', PI},
     {'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '=', '%'},
     {'0', '.', '+', MINUS, TIMES, DIV, '^', '(', ')', 'C'},        
     {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'E'},
     };
     */

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        internalConfigChange(config);
    }

    private void internalConfigChange(Configuration config) {
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        toolbar.setOnMenuItemClickListener(this);

        graphView = (GraphView) findViewById(R.id.graph);
        graph3dView = (Graph3dView) findViewById(R.id.graph3d);
        historyView = (ListView) findViewById(R.id.history);

        final boolean isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        // final boolean hasKeyboard = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;

        alpha = (KeyboardView) findViewById(R.id.alpha);
        digits = (KeyboardView) findViewById(R.id.digits);
        if (isLandscape) {
            digits.init(DIGITS2, false, true, this);
            isAlphaVisible = false;
        } else {
            alpha.init(ALPHA, false, false, this);
            digits.init(DIGITS, true, true, this);
            updateAlpha();
        }

        result = (TextView) findViewById(R.id.result);

        Editable oldText = input != null ? input.getText() : null;
        input = (EditText) findViewById(R.id.input);
        input.setOnKeyListener(this);
        input.addTextChangedListener(this);
        input.setEditableFactory(new CalculatorEditable.Factory());

        if (Build.VERSION.SDK_INT > 10) {// for greater 4.x
            input.setTextIsSelectable(true);
        } else {                         // hide keyboard for less 4.x
            input.setInputType(0);
        }

        changeInput(history.getText());
        if (oldText != null) {
            input.setText(oldText);
        }
        input.requestFocus();
//      InputMethodManager inputManager
//               = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//       inputManager.hideSoftInputFromWindow(
//              this.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_HIDDEN);
        graphView.setOnClickListener(this);
        graph3dView.setOnClickListener(this);
        if (historyView != null) {
            historyView.setAdapter(adapter);
            historyView.setOnItemClickListener(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        history = new History(this);
        adapter = new HistoryAdapter(this, history);
        internalConfigChange(getResources().getConfiguration());

        defs = new Defs(this, symbols);
        if (history.fileNotFound) {
            String[] init = {
                    "sqrt(pi)\u00f70.5!",
                    "e^(i\u00d7pi)",
                    "ln(e^100)",
                    "sin(x)",
                    "x^2"
            };
            nDigits = 10;
            for (String s : init) {
                onEnter(s);
            }
            nDigits = 0;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        String value = prefs.getString("quality", null);
        if (value == null) {
            useHighQuality3d = ua.naiksoftware.aritymod.Util.SDK_VERSION >= 5;
            prefs.edit().putString("quality", useHighQuality3d ? "high" : "low").apply();
        } else {
            useHighQuality3d = value.equals("high");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        graph3dView.onPause();
        history.updateEdited(input.getText().toString());
        history.save();
        defs.save();
    }

    @Override
    public void onResume() {
        super.onResume();
        graph3dView.onResume();
    }

    /*@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.clear_history).setEnabled(history.size() > 0);
        menu.findItem(R.id.list_defs).setEnabled(defs.size() > 0);
        return true;
    }*/

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        switch (id) {
            case R.id.list_defs: {
                Intent intent = new Intent(this, DefsActivity.class);
                intent.putExtra(DefsActivity.PARAM_DEFS, new ParcelableBinder<>(defs));
                startActivity(intent);
                break;
            }

            case R.id.help:
                startActivity(new Intent(this, HelpActivity.class));
                break;

            case R.id.clear_history:
                history.clear();
                history.save();
                adapter.notifyDataSetInvalidated();
                break;

            case R.id.clear_defs:
                defs.clear();
                defs.save();
                break;

            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;

            default:
                return false;
        }
        return true;
    }

    //OnSharedPreferenceChangeListener
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("quality")) {
            useHighQuality3d = prefs.getString(key, "high").equals("high");
            // Calculator.log("useHigh quality changed to " + useHighQuality3d);
        }
    }

    //OnClickListener
    public void onClick(View target) {
        if (target == graphView || target == graph3dView) {
            startActivity(new Intent(this, ShowGraph.class));
        }
    }

    // OnItemClickListener
    public void onItemClick(AdapterView parent, View view, int pos, long id) {
        history.moveToPos(pos, input.getText().toString());
        changeInput(history.getText());
    }

    // TextWatcher
    public void afterTextChanged(Editable s) {
        // handler.removeMessages(MSG_INPUT_CHANGED);
        // handler.sendEmptyMessageDelayed(MSG_INPUT_CHANGED, 250);
        evaluate();
        /*
         if (pendingClearResult && s.length() != 0) {
         if (!(s.length() == 4 && s.toString().startsWith("ans"))) {
         result.setText(null);
         }
         showGraph(null);
         pendingClearResult = false;
         }
         */
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
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

    /*
     private Handler handler = new Handler() {
     public void handleMessage(Message msg) {
     switch (msg.what) {                    
     case MSG_INPUT_CHANGED:
     // String text = input.getText().toString();
     evaluate();
     }
     }
     };
     */
    static void log(String mes) {
        if (true) {
            Log.d("Calculator", mes);
        }
    }

    void evaluate() {
        evaluate(input.getText().toString());
    }

    private String formatEval(Complex value) {
        if (nDigits == 0) {
            nDigits = getResultSpace();
        }
        String res = Util.complexToString(value, nDigits, 2);
        return res.replace(INFINITY, INFINITY_UNICODE);
    }

    private void evaluate(String text) {
        // log("evaluate " + text);
        if (text.length() == 0) {
            result.setEnabled(false);
            return;
        }

        auxFuncs.clear();
        int end = -1;
        do {
            text = text.substring(end + 1);
            end = text.indexOf(';');
            String slice = end == -1 ? text : text.substring(0, end);
            try {
                Function f = symbols.compile(slice);
                auxFuncs.add(f);
            } catch (SyntaxException e) {
                continue;
            }
        } while (end != -1);

        graphedFunction = auxFuncs;
        int size = auxFuncs.size();
        if (size == 0) {
            result.setEnabled(false);
            return;
        } else if (size == 1) {
            Function f = auxFuncs.get(0);
            int arity = f.arity();
            // Calculator.log("res " + f);
            if (arity == 1 || arity == 2) {
                result.setText(null);
                showGraph(f);
            } else if (arity == 0) {
                result.setText(formatEval(f.evalComplex()));
                result.setEnabled(true);
                showGraph(null);
            } else {
                result.setText("function");
                result.setEnabled(true);
                showGraph(null);
            }
        } else {
            graphView.setFunctions(auxFuncs);
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
        }
    }

    private int getResultSpace() {
        int width = result.getWidth() - result.getTotalPaddingLeft() - result.getTotalPaddingRight();
        float oneDigitWidth = result.getPaint().measureText("5555555555") / 10f;
        return (int) (width / oneDigitWidth);
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
            // graphedFunction = f;
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

    void onEnter() {
        onEnter(input.getText().toString());
    }

    void onEnter(String text) {
        boolean historyChanged = false;
        try {
            FunctionAndName fan = symbols.compileWithName(text);
            if (fan.name != null) {
                symbols.define(fan);
                defs.add(text);
            }
            Function f = fan.function;
            int arity = f.arity();
            Complex value = null;
            if (arity == 0) {
                value = f.evalComplex();
                symbols.define("ans", value);
            }
            historyChanged = arity == 0
                    ? history.onEnter(text, formatEval(value))
                    : history.onEnter(text, null);
        } catch (SyntaxException e) {
            historyChanged = history.onEnter(text, null);
        }
        showGraph(null);
        if (historyChanged) {
            adapter.notifyDataSetInvalidated();
        }
        if (text.length() == 0) {
            result.setText(null);
        }
        changeInput(history.getText());
    }

    private void changeInput(String newInput) {
        input.setText(newInput);
        input.setSelection(newInput.length());
        /*
         if (newInput.length() > 0) {
         result.setText(null);
         } else {
         pendingClearResult = true;
         }
         */
        /*
         if (result.getText().equals("function")) {
         result.setText(null);
         }
         */
    }

    /*
     private void updateChecked() {
     int pos = history.getListPos();
     if (pos >= 0) {
     log("check " + pos);
     historyView.setItemChecked(pos, true);
     adapter.notifyDataSetInvalidated();
     }
     }
     */
    void onUp() {
        if (history.moveUp(input.getText().toString())) {
            changeInput(history.getText());
            // updateChecked();
        }
    }

    void onDown() {
        if (history.moveDown(input.getText().toString())) {
            changeInput(history.getText());
            // updateChecked();
        }
    }

    private static final KeyEvent KEY_DEL = new KeyEvent(0, KeyEvent.KEYCODE_DEL),
            KEY_ENTER = new KeyEvent(0, KeyEvent.KEYCODE_ENTER);

    void doEnter() {
        onEnter();
    }

    void doBackspace() {
        input.dispatchKeyEvent(KEY_DEL);
    }

}
