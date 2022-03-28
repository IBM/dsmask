/*
 * Copyright (c) IBM Corp. 2018, 2022.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Maksim Zinal (IBM) - Initial implementation
 */
package net.dsmask.engine;

/**
 *
 * @author zinal
 */
public class MaskingJob implements Runnable {

    private final LinkInput input;
    private final LinkOutput output;
    private final MaskingEngine engine;

    public MaskingJob(Workspace workspace) {
        this.input = workspace.getInputLink();
        this.output = workspace.getOutputLink();
        this.engine = new MaskingEngine(workspace);
    }

    @Override
    public void run() {
        // read input records one by one
        RowInput row;
        while ((row = input.readRecord()) != null) {
            // add each record to the engine for processing
            if (! engine.addRow(row)) {
                // If the row cannot be added, mask the data.
                maskAndSend();
                // Add the row before moving forward.
                engine.addRow(row);
            }
        }
        // Mask the last couple of rows.
        maskAndSend();
    }

    private void maskAndSend() {
        // compute the masking results
        engine.process();
        // send the results to the output records destination
        final int nrows = engine.getRowCount();
        for (int i=0; i<nrows; ++i) {
            engine.collect(i, output);
        }
        // reset the engine for the next batch of records
        engine.reset();
    }

}
