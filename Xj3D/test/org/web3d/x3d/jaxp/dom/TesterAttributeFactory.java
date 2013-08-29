/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.x3d.jaxp.dom;

/**
 * Implementation of the {@link AttributeFactory} interface to be used for
 * purely testing purposes.
 * <p>
 * This does no checking when creating an attribute and just returns default
 * instances of X3DAttr();
 */
class TesterAttributeFactory implements AttributeFactory {

    /**
     * Create an attribute that does not belong to a namespace. If the factory
     * does not know how to create an attribute of this type then it must
     * return null. If there is an error creating an attribute of this type, it
     * shall be treated the same as not knowing how to create it
     *
     * @param element The element this attribute belongs to
     * @param name The name of the attribute
     * @param value The value of the attribute (may be null)
     * @return An attribute that represents the name or null
     */
    public X3DAttr createAttribute(X3DElement element,
                                   String name,
                                   String value) {
        return new X3DAttr(name, value);
    }

    /**
     * Create an element that belongs to the given namespace. If the factory
     * does not know how to create an element of this type then it must
     * return null. If there is an error creating an element of this type, it
     * shall be treated the same as not knowing how to create it. The namespace
     * may be different to that of the parent element.
     *
     * @param element The element this attribute belongs to
     * @param namespace The namespace for the attribute to exist in
     * @param name The name of the attribute
     * @param value The value of the attribute (may be null)
     * @return An attribute that represents the name or null
     */
    public X3DAttr createAttribute(X3DElement element,
                                   String namespace,
                                   String name,
                                   String value) {
        return new X3DAttr(namespace, name, value);
    }
}
