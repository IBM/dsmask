/*
 * Copyright (c) IBM Corp. 2018, 2021.
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
package net.dsmask.model.xml;

import java.util.Map;
import org.jdom2.CDATA;
import org.jdom2.Element;
import org.apache.commons.lang3.StringUtils;
import net.dsmask.model.*;

/**
 * Generate XML DOM tree data from model entity beans.
 * @author zinal
 */
public class ModelDumper extends XmlNames {

    /**
     * Create an XML DOM tree for a single model entity bean.
     * @param me Model entity bean.
     * @return Generated XML DOM tree.
     */
    public Element dump(ModelEntity me) {
        switch (me.getEntityType()) {
            case Fragment:
                return dumpFragment((MaskingFragment) me);
            case Function:
                return dumpFunction((MaskingFunction) me);
            case Key:
                return dumpKey((MaskingKey) me);
            case Label:
                return dumpLabel((MaskingLabel) me);
            case Selector:
                return dumpSelector((LabelSelector) me);
            case Metadata:
                return dumpMetadata((MetaEntity) me);
            case Rule:
                return dumpRule((MaskingRule) me);
            case Profile:
                return dumpProfile((MaskingProfile) me);
        }
        throw new IllegalArgumentException(me.toString());
    }

    /**
     * Generate XML DOM trees for all model entity beans which are listed
     * by the specified model accessor, and write each one to the repository.
     * @param ma Model accessor
     * @param xod XML data reposititory output helper
     */
    public void dump(ModelAccessor ma, XmlDeployer xod) {
        for (EntityType et : EntityType.values()) {
            ma.list(et).forEach(mn -> {
                ModelEntity me = ma.find(mn);
                if (me!=null) {
                    xod.save(mn, dump(me));
                }
            });
        }
    }

    private Element elementEntity(ModelEntity me) {
        Element el = new Element(me.getEntityType().tag);
        el.setAttribute(ATT_NAME, me.getName());
        return el;
    }

    private Element dumpFragment(MaskingFragment x) {
        Element el = elementEntity(x);
        Element elSeq = dumpItemSequence(x.getPipeline());
        el.addContent(elSeq);
        return el;
    }

    private static boolean isSimpleString(String s) {
        if (s==null)
            return true;
        if (s.length() > 30)
            return false;
        for (int i=0; i<s.length(); ++i) {
            char c = s.charAt(i);
            if (! ( (c >= 0x20 && c <= 0x7E)
                    || Character.isSpaceChar(c)
                    || Character.isLetterOrDigit(c)) )
                return false;
        }
        return true;
    }

    private Element dumpFunction(MaskingFunction x) {
        Element el = elementEntity(x);
        el.setAttribute(ATT_ALGO, x.getAlgorithm().getName());
        for (Map.Entry<String,String> me : x.getParameters().entrySet()) {
            Element elParam = new Element(TAG_FuncParam);
            elParam.setAttribute(ATT_NAME, me.getKey());
            if (isSimpleString(me.getValue())) {
                elParam.setAttribute(ATT_VALUE, me.getValue());
            } else {
                elParam.addContent(new CDATA(me.getValue()));
            }
            el.addContent(elParam);
        }
        return el;
    }

    private Element dumpKey(MaskingKey x) {
        Element el = elementEntity(x);
        el.setAttribute(ATT_VALUE, x.getValue());
        return el;
    }

    private Element dumpLabel(MaskingLabel x) {
        Element el = elementEntity(x);
        el.setAttribute(ATT_MODE, x.getMode().name());
        return el;
    }

    private Element dumpMetadata(MetaEntity x) {
        Element el = elementEntity(x);
        Element elTab = dumpMetaTable(x.getTable());
        el.addContent(elTab);
        return el;
    }

    private Element dumpMetaTable(MetaTable mt) {
        Element el = new Element(TAG_TableInfo);
        if (! StringUtils.isBlank(mt.getDatabaseName()) )
            el.setAttribute(ATT_DB, mt.getDatabaseName());
        if (! StringUtils.isBlank(mt.getSchemaName()) )
            el.setAttribute(ATT_SCHEMA, mt.getSchemaName());
        el.setAttribute(ATT_TABLE, mt.getTableName());
        el.setAttribute(ATT_CI, mt.isIgnoreCase() ? "T" : "F");
        for (MetaField mf : mt.getFields()) {
            Element elField = dumpMetaField(mf);
            el.addContent(elField);
        }
        return el;
    }

    private Element dumpMetaField(MetaField mf) {
        Element el = new Element(TAG_Field);
        el.setAttribute(ATT_NAME, mf.getName());
        for (String tag : mf.getPublicTags()) {
            Element elTag = new Element(TAG_FieldTag);
            elTag.setAttribute(ATT_NAME, tag);
            el.addContent(elTag);
        }
        for (MaskingLabel ml : mf.getLabels()) {
            Element elLabel = new Element(TAG_FieldLabel);
            elLabel.setAttribute(ATT_NAME, ml.getName());
            el.addContent(elLabel);
        }
        return el;
    }

    private Element dumpRule(MaskingRule x) {
        Element el = elementEntity(x);
        Element elMeta = new Element(TAG_RuleMeta);
        for (String context : x.getContexts()) {
            Element elCtx = new Element(TAG_RuleCtx);
            elCtx.setAttribute(ATT_NAME, context);
            elMeta.addContent(elCtx);
        }
        for (MetaReference mr : x.getInputs()) {
            Element elIn = dumpRuleLabel(mr, TAG_RuleIn);
            elMeta.addContent(elIn);
        }
        for (MetaReference mr : x.getOutputs()) {
            Element elOut = dumpRuleLabel(mr, TAG_RuleOut);
            elMeta.addContent(elOut);
        }
        el.addContent(elMeta);
        // common item sequence logic for rules and fragments
        Element elSeq = dumpItemSequence(x.getPipeline());
        if (elSeq != null)
            el.addContent(elSeq);
        return el;
    }

    private Element dumpRuleLabel(MetaReference mr, String tagName) {
        Element el = new Element(tagName);
        for (MaskingLabel ml : mr.getLabels()) {
            Element elLab = new Element(TAG_RuleLabel);
            elLab.setAttribute(ATT_NAME, ml.getName());
            el.addContent(elLab);
        }
        return el;
    }

    private Element dumpItemSequence(StepGroup block) {
        int counter = 0;
        Element el = new Element(TAG_ItemSequence);
        for (StepBase item : block.getItems()) {
            Element elItem = null;
            switch (item.getType()) {
                case Function:
                    elItem = dumpStepFunction((StepFunction) item);
                    break;
                case Script:
                    elItem = dumpStepScript((StepScript) item);
                    break;
                case Block:
                    elItem = dumpStepBlock((StepBlock) item);
                    break;
                case Fragment:
                    elItem = dumpStepFragment((StepFragment) item);
                    break;
                case Root: /* noop */
                    break;
            }
            if (elItem != null) {
                el.addContent(elItem);
                ++counter;
            }
        }
        return (counter == 0) ? null : el;
    }

    private Element dumpStepBase(StepBase x, String tagName) {
        Element el = new Element(tagName);
        el.setAttribute(ATT_NAME, x.getName());
        if (! (x.getPredicates().isEmpty() && x.getInputs().isEmpty()) ) {
            Element elMeta = new Element(TAG_ItemMeta);
            for (ValueRef vr : x.getPredicates()) {
                elMeta.addContent(dumpReference(vr, TAG_ItemPred));
            }
            for (ValueRef vr : x.getInputs()) {
                elMeta.addContent(dumpReference(vr, TAG_ItemArg));
            }
            el.addContent(elMeta);
        }
        if (x.getUniqCheck() != null) {
            el.addContent(dumpUniqCheck(x.getUniqCheck()));
        }
        return el;
    }

    private Element dumpReference(ValueRef vr, String tagName) {
        Element el = new Element(tagName);
        /* TODO: implementation
        el.setAttribute(ATT_NAME, vr.getItem().getName());
        el.setAttribute(ATT_POS, String.valueOf(vr.getPosition()));
        */
        return el;
    }

    private Element dumpUniqCheck(UniqCheck uc) {
        Element el = new Element(TAG_Uniq);
        el.setAttribute(ATT_PROV, uc.getProvider());
        if (uc.hasInputPositions()) {
            for (int ix : uc.getInputPositions()) {
                Element elIx = new Element(TAG_UniqIn);
                elIx.setAttribute(ATT_POS, String.valueOf(ix));
                el.addContent(elIx);
            }
        }
        if (uc.hasOutputPositions()) {
            for (int ix : uc.getOutputPositions()) {
                Element elIx = new Element(TAG_UniqOut);
                elIx.setAttribute(ATT_POS, String.valueOf(ix));
                el.addContent(elIx);
            }
        }
        return el;
    }

    private Element dumpStepFunction(StepFunction x) {
        Element el = dumpStepBase(x, TAG_StepFunc);
        el.setAttribute(ATT_FUNC, x.getFunction().getName());
        return el;
    }

    private Element dumpStepScript(StepScript x) {
        Element el = dumpStepBase(x, TAG_StepScript);
        if (! StringUtils.isBlank(x.getBody()) ) {
            Element elBody = new Element(TAG_ScriptBody);
            elBody.addContent(new CDATA(x.getBody()));
            el.addContent(elBody);
        }
        return el;
    }

    private Element dumpStepBlock(StepBlock x) {
        Element el = dumpStepBase(x, TAG_StepBlock);
        Element elSeq = dumpItemSequence(x);
        if (elSeq != null)
            el.addContent(elSeq);
        return el;
    }

    private Element dumpStepFragment(StepFragment x) {
        Element el = dumpStepBase(x, TAG_StepFragment);
        if ( x.getFragment() != null )
            el.setAttribute(ATT_FRAGM, x.getFragment().getName());
        return el;
    }

    private Element dumpSelector(LabelSelector x) {
        Element el = elementEntity(x);
        for (LabelSelector.Item i : x.getItems()) {
            Element item = new Element(TAG_SelectorItem);
            item.setAttribute(ATT_SOURCE, i.source);
            item.setAttribute(ATT_TARGET, i.target);
            item.setAttribute(ATT_IS_RX, i.rx ? "T" : "F");
            el.addContent(item);
        }
        return el;
    }

    private Element dumpProfile(MaskingProfile x) {
        if (x.getTable()==null)
            return null;
        Element el = new Element(TAG_Profile);
        AnyTable mt = x.getTable();
        el.setAttribute(ATT_TABLE, mt.isIgnoreCase() ?
                mt.getFullName().toLowerCase() : mt.getFullName());
        for (MaskingOperation op : x.getOperations()) {
            Element elOp = dumpOperation(op);
            el.addContent(elOp);
        }
        return el;
    }

    private Element dumpOperation(MaskingOperation op) {
        Element el = new Element(TAG_Operation);
        el.setAttribute(ATT_RULE, op.getRule().getName());
        for (AnyField mf : op.getInputs()) {
            Element elInput = new Element(TAG_OperationIn);
            elInput.setAttribute(ATT_FIELD, mf.getName());
            el.addContent(elInput);
        }
        for (AnyField mf : op.getOutputs()) {
            Element elInput = new Element(TAG_OperationOut);
            elInput.setAttribute(ATT_FIELD, mf.getName());
            el.addContent(elInput);
        }
        return el;
    }

}
