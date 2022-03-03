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
package net.dsmask.beans.xml;

import net.dsmask.beans.AlgorithmInfo;
import net.dsmask.beans.AlgorithmProvider;
import net.dsmask.model.MaskingProfile;
import net.dsmask.model.MaskingOperation;
import net.dsmask.model.LabelMode;
import net.dsmask.model.ModelUtils;
import net.dsmask.beans.ItemBase;
import net.dsmask.beans.ModelName;
import net.dsmask.beans.MaskingKey;
import net.dsmask.beans.ItemStep;
import net.dsmask.beans.MaskingRule;
import net.dsmask.beans.MetaTable;
import net.dsmask.beans.UniqCheck;
import net.dsmask.beans.ItemBlock;
import net.dsmask.beans.ValueRef;
import net.dsmask.beans.MetaField;
import net.dsmask.beans.EntityType;
import net.dsmask.beans.LabelSelector;
import net.dsmask.beans.PackageAccessor;
import net.dsmask.beans.MaskingFunction;
import net.dsmask.beans.MaskingConfig;
import net.dsmask.beans.ItemFragment;
import net.dsmask.beans.MaskingLabel;
import net.dsmask.beans.MaskingFragment;
import net.dsmask.beans.ModelPackage;
import net.dsmask.beans.ItemScript;
import net.dsmask.beans.MetaEntity;
import net.dsmask.beans.MetaReference;
import net.dsmask.beans.ModelEntity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Element;

/**
 * Incremental model package loader implementation, with caching.
 * @author zinal
 */
public class ModelLoader extends XmlNames implements PackageAccessor {

    protected final XmlObjectProvider provider;
    protected ModelPackage pool = null;
    private final Deque<XmlObject> stack = new ArrayDeque<>();

    public ModelLoader(XmlObjectProvider provider) {
        this.provider = provider;
        this.initParser();
    }

    protected final void initParser() {
        stack.clear();
        pool = new ModelPackage(provider.getPackageName());
    }

    @Override
    public String getName() {
        return provider.getPackageName();
    }

    @Override
    public String getId() {
        return ModelUtils.lower(provider.getPackageName());
    }

    @Override
    public ModelEntity find(EntityType type, String name) {
        if (pool==null)
            throw new IllegalStateException();
        ModelEntity me = pool.find(type, name);
        if (me==null) {
            XmlObject xo = provider.getObject(type, name);
            if (xo==null)
                return null;
            handleEntity(xo);
            me = pool.find(type, name);
        }
        return me;
    }

    @Override
    public ModelEntity find(ModelName mn) {
        if (mn==null)
            return null;
        return find(mn.getEntityType(), mn.getName());
    }

    @Override
    public <ET extends ModelEntity> ET find(Class<ET> type, String name) {
        EntityType et = EntityType.CLAZZ.get(type);
        if (et == null)
            return null;
        @SuppressWarnings("unchecked")
        ET retval = (ET) find(et, name);
        return retval;
    }

    @Override
    public Collection<ModelName> list() {
        return Collections.unmodifiableCollection(provider.enumObjects() );
    }

    @Override
    public Collection<ModelName> list(EntityType type) {
        return Collections.unmodifiableCollection(provider.enumObjects(type) );
    }

    /**
     * Resolves the reference to other model entity, parsing it if necessary.
     * Throws exception if the reference is not valid, or is not present.
     * @param <ET> Java object type
     * @param type Entity type identifier (should match the Java object type)
     * @param el JDOM node containing the reference
     * @param attr Name of the attribute containing the reference
     * @return Model entity object.
     */
    protected final <ET extends ModelEntity>
    ET resolve(EntityType type, Element el, String attr) {
        return resolve(type, el, attr, false);
    }

    /**
     * Resolves the reference to other model entity, parsing it if necessary.
     * Throws exception if the reference is not valid.
     * @param <ET> Java object type
     * @param type Entity type identifier (should match the Java object type)
     * @param el JDOM node containing the reference
     * @param attr Name of the attribute containing the reference
     * @param optional true, if the reference is optional (can be missing), false otherwise
     * @return Model entity object.
     */
    protected final <ET extends ModelEntity>
    ET resolve(EntityType type, Element el, String attr, boolean optional) {
        String ref = optional ? XmlObject.getAttr(el, attr, null) : XmlObject.getAttr(el, attr);
        if (ref==null) // only possible if optional==true
            return null;
        @SuppressWarnings("unchecked")
        ET me = (ET) find(type, ref);
        if (me==null) {
            throw XmlObject.raise(el, "Invalid reference [" + type.name()
                    + ":" + ref + "] from tag '" + el.getName() + "'");
        }
        return me;
    }

    /**
     * Validate that object processing has not been started yet.
     * @param xo XML object to be processed.
     */
    protected void ensureObjectIsNew(XmlObject xo) {
        List<XmlObject> found = null;
        for (XmlObject other : stack) {
            if (found != null) {
                found.add(xo);
            } else {
                if (xo == other)
                    found = new ArrayList<>();
            }
        }
        if (found != null) {
            final StringBuilder msg = new StringBuilder();
            msg .append("Referential cycle ")
                .append(xo.getEntityType().name()).append(':')
                .append(xo.getElement().getAttributeValue(ATT_NAME));
            for (XmlObject other : found) {
                msg .append(" -> ")
                    .append(other.getEntityType().name()).append(':')
                    .append(other.getElement().getAttributeValue(ATT_NAME));
            }
            msg .append(" -> ")
                .append(xo.getEntityType().name()).append(':')
                .append(xo.getElement().getAttributeValue(ATT_NAME));
            // signal the error through exception
            throw xo.raise(msg);
        }
    }

    /**
     * Build the model entity from the XML object represented by a JDOM node,
     * and add the new entity to the pool.
     * This method recursively parses the references, by calling the loadEntity() method.
     * Referential cycles are detected through the use of object stack.
     * @param xo XML object to be parsed.
     */
    protected void handleEntity(XmlObject xo) {
        // fail on object which we are already processing
        ensureObjectIsNew(xo);
        // starting object processing
        stack.push(xo);
        switch (xo.getEntityType()) {
            case Key:
                handleKey(xo);
                break;
            case Function:
                handleFunction(xo);
                break;
            case Fragment:
                handleFragment(xo);
                break;
            case Rule:
                handleRule(xo);
                break;
            case Label:
                handleLabel(xo);
                break;
            case Selector:
                handleSelector(xo);
                break;
            case Metadata:
                handleMetadata(xo);
                break;
            case Config:
                handleConfig(xo);
                break;
        }
        // completed object processing
        stack.pop();
    }

    /* --------------------------- */
    /* parsing: initialization key */

    private void handleKey(XmlObject xo) {
        MaskingKey e = new MaskingKey(xo.getName(), xo.getAttr(ATT_VALUE));
        pool.addEntry(e);
    }

    /* --------------------------- */
    /* parsing: function */

    private void handleFunction(XmlObject xo) {
        String algoName = XmlObject.getAttr(xo.getElement(), ATT_ALGO);
        AlgorithmInfo algo = AlgorithmProvider.getInstance().findAlgorithm(algoName);
        if (algo == null)
            throw XmlObject.raiseIllegal(xo.getElement(), ATT_ALGO);
        MaskingFunction e = new MaskingFunction(xo.getName(), algo);
        for (Element param : xo.getChildren(TAG_FuncParam)) {
            String pname = XmlObject.getAttr(param, ATT_NAME);
            String pvalue = XmlObject.getAttr(param, ATT_VALUE, null);
            if (pvalue == null) {
                pvalue = XmlObject.getText(param);
            }
            e.setParameter(pname, pvalue);
        }
        pool.addEntry(e);
    }

    /* --------------------------- */
    /* parsing: fragment */

    private void handleFragment(XmlObject xo) {
        MaskingFragment e = new MaskingFragment(xo.getName());
        handleStepSequence(e.getPipeline(), xo.getElement());
        pool.addEntry(e);
    }

    private void handleStepSequence(ItemBlock owner, Element elBase) {
        for (Element elSeq : elBase.getChildren(TAG_ItemSequence)) {
            for (Element elStep : elSeq.getChildren()) {
                ItemBase item = null;
                if ( TAG_ItemFunc.equalsIgnoreCase(elStep.getName()) ) {
                    item = makeItemFunction(owner, elStep);
                } else if ( TAG_ItemScript.equalsIgnoreCase(elStep.getName()) ) {
                    item = makeItemScript(owner, elStep);
                } else if ( TAG_ItemBlock.equalsIgnoreCase(elStep.getName()) ) {
                    item = makeItemBlock(owner, elStep);
                } else if (TAG_ItemFragment.equalsIgnoreCase(elStep.getName()) ) {
                    item = makeItemFragment(owner, elStep);
                }
                if (item != null)
                    owner.addItem(item);
            }
        }
    }

    private String getScriptBody(Element el) {
        for ( Element x : el.getChildren(TAG_ScriptBody) ) {
            String body = XmlObject.getText(x);
            if (StringUtils.isBlank(body)) {
                throw XmlObject.raise(el, "Empty script body");
            }
            return body;
        }
        throw XmlObject.raise(el, "Missing script body element");
    }

    private void fillStep(ItemBlock owner, ItemBase item, Element el) {
        for ( Element x : el.getChildren(TAG_ItemMeta) ) {
            if ( TAG_ItemArg.equalsIgnoreCase(x.getName()) ) {
                fillReference(owner, x, item.getInputs());
            } else if ( TAG_ItemPred.equalsIgnoreCase(x.getName()) ) {
                fillReference(owner, x, item.getPredicates());
            }
        }
        for ( Element x : el.getChildren(TAG_Uniq) ) {
            fillUniqCheck(item, x);
        }
    }

    private void fillReference(ItemBlock owner, Element x, List<ValueRef> items) {
        // here name is optional, and can start with illegal characters
        String name = XmlObject.getAttr(x, ATT_NAME, null);
        int position = XmlObject.getInt(x, ATT_POS);
        if (position < 1 || position > 1000) {
            throw XmlObject.raiseIllegal(x, ATT_POS);
        }
        ValueRef ref = new ValueRef(owner.findItem(name), position);
        if (ref.getItem() == null) {
            throw XmlObject.raiseIllegal(x, ATT_NAME);
        }
        items.add(ref);
    }

    private void fillUniqCheck(ItemBase item, Element x) {
        final UniqCheck uc = new UniqCheck(XmlObject.getAttr(x, ATT_PROV));
        uc.setInputPositions(grabIndexes(x.getChildren(TAG_UniqIn)));
        uc.setInputPositions(grabIndexes(x.getChildren(TAG_UniqOut)));
        item.setUniqCheck(uc);
    }

    private static int[] grabIndexes(List<Element> vals) {
        if (vals==null || vals.isEmpty())
            return null;
        int[] retval = new int[vals.size()];
        for (int i=0; i<vals.size(); ++i) {
            Element el = vals.get(i);
            String index = el.getAttributeValue(ATT_POS);
            if (index==null)
                index = "1";
            final int ix;
            try {
                ix = Integer.parseInt(index) - 1;
            } catch(NumberFormatException nfe) {
                throw XmlObject.raiseIllegal(el, ATT_POS);
            }
            if (ix < 0 || ix > 1000) {
                throw XmlObject.raiseIllegal(el, ATT_POS);
            }
            retval[i] = ix;
        }
        return retval;
    }

    private ItemStep makeItemFunction(ItemBlock owner, Element el) {
        MaskingFunction f = resolve(EntityType.Function, el, ATT_FUNC);
        ItemStep i = new ItemStep(XmlObject.getName(el), f);
        fillStep(owner, i, el);
        return i;
    }

    private ItemScript makeItemScript(ItemBlock owner, Element el) {
        ItemScript i = new ItemScript(XmlObject.getName(el), getScriptBody(el));
        fillStep(owner, i, el);
        return i;
    }

    private ItemBlock makeItemBlock(ItemBlock owner, Element el) {
        ItemBlock i = new ItemBlock(XmlObject.getName(el), owner);
        handleStepSequence(i, el);
        fillStep(owner, i, el);
        return i;
    }

    private ItemFragment makeItemFragment(ItemBlock owner, Element el) {
        MaskingFragment f = resolve(EntityType.Fragment, el, ATT_FRAGM);
        ItemFragment i = new ItemFragment(XmlObject.getName(el), f);
        fillStep(owner, i, el);
        return i;
    }

    /* --------------------------- */
    /* parsing: rule */

    private void handleRule(XmlObject xo) {
        MaskingRule r = new MaskingRule(xo.getName());
        // process the rule input and output metadata
        for (Element elMeta : xo.getChildren(TAG_RuleMeta)) {
            for (Element el : elMeta.getChildren()) {
                if (TAG_RuleIn.equalsIgnoreCase(el.getName())) {
                    MetaReference mr = makeMetaReference(el);
                    r.getInputs().add(mr);
                } else if (TAG_RuleOut.equalsIgnoreCase(el.getName())) {
                    MetaReference mr = makeMetaReference(el);
                    r.getOutputs().add(mr);
                } else if (TAG_RuleCtx.equalsIgnoreCase(el.getName())) {
                    r.getContexts().add(XmlObject.getName(el));
                } else {
                    throw XmlObject.raise(el, "Unknown tag '" + el.getName() + "'");
                }
            }
        }
        // re-use the step parsing logic from the fragment parser
        handleStepSequence(r.getPipeline(), xo.getElement());
        pool.addEntry(r);
    }

    private MetaReference makeMetaReference(Element el) {
        MetaReference mr = new MetaReference();
        for (Element x : el.getChildren(TAG_RuleLabel)) {
            MaskingLabel label = resolve(EntityType.Label, x, ATT_NAME);
            mr.addLabel(label);
        }
        return mr;
    }

    /* --------------------------- */
    /* parsing: label */

    private void handleLabel(XmlObject xo) {
        MaskingLabel l = new MaskingLabel(xo.getName(),
                getMetaMode(xo.getElement(), ATT_MODE));
        pool.addEntry(l);
    }

    private static LabelMode getMetaMode(Element el, String attr) {
        final String value = el.getAttributeValue(attr);
        if (StringUtils.isBlank(value))
            return LabelMode.Normal;
        final String xvalue = value.trim().toUpperCase();
        if (xvalue.length()==0)
            return LabelMode.Normal;
        for (LabelMode lm : LabelMode.values()) {
            if (lm.getCode().charAt(0) == xvalue.charAt(0))
                return lm;
        }
        throw XmlObject.raiseIllegal(el, attr);
    }

    /* --------------------------- */
    /* parsing: table metadata */

    private void handleMetadata(XmlObject xo) {
        Element elTable = null;
        for (Element el : xo.getChildren(TAG_TableInfo)) {
            if (elTable != null) {
                throw XmlObject.raise(el, "Unexpected multiple tags " + TAG_TableInfo);
            }
            elTable = el;
        }
        if (elTable==null) {
            throw xo.raise("Missing tag " + TAG_TableInfo);
        }
        MetaEntity m = new MetaEntity( makeMetaTable(elTable) );
        pool.addEntry(m);
    }

    private MetaTable makeMetaTable(Element el) {
        // handle variants of table name representation
        String tableName = XmlObject.getAttr(el, ATT_TABLE);
        String schemaName = XmlObject.getAttr(el, ATT_SCHEMA, null);
        String dbName = XmlObject.getAttr(el, ATT_DB, null);
        boolean ignoreCase = XmlObject.getBool(el, ATT_CI, false);
        if (StringUtils.isBlank(tableName)) {
            throw XmlObject.raiseIllegal(el, ATT_TABLE);
        }
        tableName = tableName.trim();
        final MetaTable mt;
        if (StringUtils.isBlank(dbName)) {
            if (StringUtils.isBlank(schemaName)) {
                mt = new MetaTable(tableName, ignoreCase);
            } else {
                schemaName = schemaName.trim();
                mt = new MetaTable(null, schemaName, tableName, ignoreCase);
            }
        } else {
            dbName = dbName.trim();
            if (StringUtils.isBlank(schemaName)) {
                mt = new MetaTable(dbName, tableName, ignoreCase);
            } else {
                schemaName = schemaName.trim();
                mt = new MetaTable(dbName, schemaName, tableName, ignoreCase);
            }
        }
        // handle fields and their labels
        for (Element x : el.getChildren(TAG_Field)) {
            MetaField mf = makeMetaField(x);
            mt.addField(mf);
        }
        return mt;
    }

    private MetaField makeMetaField(Element el) {
        final MetaField mf = new MetaField(XmlObject.getAttr(el, ATT_NAME));
        for (Element x : el.getChildren(TAG_FieldLabel)) {
            MaskingLabel label = resolve(EntityType.Label, x, ATT_NAME);
            mf.addLabel(label);
        }
        return mf;
    }

    private void handleSelector(XmlObject xo) {
        LabelSelector x = new LabelSelector(xo.getName());
        for (Element el : xo.getChildren(TAG_SelectorItem)) {
            LabelSelector.Item i = new LabelSelector.Item(
                    XmlObject.getAttr(el, ATT_SOURCE),
                    XmlObject.getAttr(el, ATT_TARGET),
                    XmlObject.getBool(el, ATT_IS_RX));
            x.getItems().add(i);
        }
        pool.addEntry(x);
    }

    private void handleConfig(XmlObject xo) {
        final MaskingConfig mc = new MaskingConfig(xo.getName());
        for (Element el : xo.getChildren(TAG_Profile)) {
            MetaEntity me = resolve(EntityType.Metadata, el, ATT_TABLE);
            MaskingProfile mp = makeProfile(me.getTable(), el);
            mc.getProfiles().add(mp);
        }
        pool.addEntry(mc);
    }

    private MaskingProfile makeProfile(MetaTable table, Element el) {
        MaskingProfile mp = new MaskingProfile(table);
        for (Element elOp : el.getChildren(TAG_Operation)) {
            MaskingOperation op = makeOperation(table, elOp);
            mp.getOperations().add(op);
        }
        return mp;
    }

    private MaskingOperation makeOperation(MetaTable table, Element el) {
        MaskingRule rule = resolve(EntityType.Rule, el, ATT_RULE);
        MaskingOperation op = new MaskingOperation(rule);
        for (Element elIn : el.getChildren(TAG_OperationIn)) {
            MetaField mf = table.findField(XmlObject.getAttr(elIn, ATT_FIELD));
            if (mf==null) {
                throw XmlObject.raiseIllegal(elIn, ATT_FIELD);
            }
            op.getInputs().add(mf);
        }
        for (Element elOut : el.getChildren(TAG_OperationOut)) {
            MetaField mf = table.findField(XmlObject.getAttr(elOut, ATT_FIELD));
            if (mf==null) {
                throw XmlObject.raiseIllegal(elOut, ATT_FIELD);
            }
            op.getOutputs().add(mf);
        }
        return op;
    }

}
