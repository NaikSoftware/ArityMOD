// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.core.util;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class FileHandler {

    private String fileName;
    private Context context;
    private int version;

    private DataInputStream openInput() {
        try {
            return new DataInputStream(new BufferedInputStream(context.openFileInput(fileName), 256));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private DataOutputStream openOutput() throws IOException {
        return new DataOutputStream(new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE), 256));
    }

    public FileHandler(Context context, String fileName, int version) {
        this.context = context;
        this.fileName = fileName;
        this.version = version;
    }

    public void load() {
        try {
            DataInputStream is = openInput();
            if (is != null) {
                int readVersion = is.readInt();
                if (readVersion != version) {
                    throw new IllegalStateException("invalid version " + readVersion);
                }
                doRead(is);
                is.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try {
            DataOutputStream os = openOutput();
            os.writeInt(version);
            doWrite(os);
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void doRead(DataInputStream is) throws IOException;

    protected abstract void doWrite(DataOutputStream os) throws IOException;
}
