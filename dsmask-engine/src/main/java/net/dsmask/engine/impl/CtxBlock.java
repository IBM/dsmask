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
package net.dsmask.engine.impl;

import net.dsmask.engine.*;
import net.dsmask.model.*;

/**
 * Masking pipeline processing context.
 * @author zinal
 */
public class CtxBlock extends CtxBase {

    private final ItemBlock pipeline;
    private final CtxBase[] items;
    private int position;

    /**
     * Helper constructor to produce the block processing context from the masking rule.
     * @param rule Masking rule (should be MaskingRule compatible).
     */
    public CtxBlock(AnyRule rule) {
        this( ((MaskingRule) rule).getPipeline() );
    }

    /**
     * Build the pipeline processing context.
     * This constructor is recursive, e.g. it generates sub-objects to process sub-pipelines.
     * @param pipeline Masking pipeline
     */
    public CtxBlock(ItemBlock pipeline) {
        super(ItemType.Block, pipeline.getInputs().size());
        this.pipeline = pipeline;
        this.items = new CtxBase[pipeline.getItems().size()];
        int pos = 0;
        for (ItemBase item : pipeline.getItems()) {
            switch (item.getType()) {
                case Block:
                    items[pos] = new CtxBlock((ItemBlock) item);
                    break;
                case Fragment:
                    items[pos] = new CtxBlock(
                            ((ItemFragment) item).getFragment().getPipeline()
                    );
                    break;
                case Script:
                    items[pos] = new CtxScript((ItemScript) item);
                    break;
                case Step:
                    items[pos] = new CtxStep((ItemStep) item);
                    break;
            }
            ++pos;
        }
    }

    public final ItemBlock getPipeline() {
        return pipeline;
    }

    public final CtxBase[] getItems() {
        return items;
    }

    public final int getPosition() {
        return position;
    }

    public final void setPosition(int position) {
        this.position = position;
    }

    public final void setup() {
        position = 0;
        for (CtxBase ctx : items) {
            switch (ctx.getItemType()) {
                case Block:
                case Fragment:
                    ((CtxBlock)ctx).setup();
                    break;
                default: /* noop */ ;
            }
        }
    }

    public final void collect(int[] index, RowOutput row) {
        // TODO
    }

}
