/*
 * collectd/java - org/collectd/java/GenericJMXConfMBean.java
 * Copyright (C) 2009  Florian octo Forster
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; only version 2 of the License is applicable.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 *
 * Authors:
 *   Florian octo Forster <octo at verplant.org>
 */

package org.collectd.java;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;


import org.collectd.api.Collectd;
import org.collectd.api.PluginData;
import org.collectd.api.OConfigValue;
import org.collectd.api.OConfigItem;

class GenericJMXConfMBean
{
  private String _name; /* name by which this mapping is referenced */
  private ObjectName _obj_name;
  private String _instance;
  private List<GenericJMXConfValue> _values;

  private String getConfigString (OConfigItem ci) /* {{{ */
  {
    List<OConfigValue> values;
    OConfigValue v;

    values = ci.getValues ();
    if (values.size () != 1)
    {
      Collectd.logError ("GenericJMXConfMBean: The " + ci.getKey ()
          + " configuration option needs exactly one string argument.");
      return (null);
    }

    v = values.get (0);
    if (v.getType () != OConfigValue.OCONFIG_TYPE_STRING)
    {
      Collectd.logError ("GenericJMXConfMBean: The " + ci.getKey ()
          + " configuration option needs exactly one string argument.");
      return (null);
    }

    return (v.getString ());
  } /* }}} String getConfigString */

/*
 * <MBean "alias name">
 *   Instance "foobar"
 *   ObjectName "object name"
 *   <Value />
 *   <Value />
 *   :
 * </MBean>
 */
  public GenericJMXConfMBean (OConfigItem ci) /* {{{ */
    throws IllegalArgumentException
  {
    List<OConfigItem> children;
    Iterator<OConfigItem> iter;

    this._name = getConfigString (ci);
    if (this._name == null)
      throw (new IllegalArgumentException ("No alias name was defined. "
            + "MBean blocks need exactly one string argument."));

    this._obj_name = null;
    this._values = new ArrayList<GenericJMXConfValue> ();

    children = ci.getChildren ();
    iter = children.iterator ();
    while (iter.hasNext ())
    {
      OConfigItem child = iter.next ();

      Collectd.logDebug ("GenericJMXConfMBean: child.getKey () = "
          + child.getKey ());
      if (child.getKey ().equalsIgnoreCase ("Instance"))
      {
        String tmp = getConfigString (child);
        if (tmp != null)
          this._instance = tmp;
      }
      else if (child.getKey ().equalsIgnoreCase ("ObjectName"))
      {
        String tmp = getConfigString (child);
        if (tmp == null)
          continue;

        try
        {
          this._obj_name = new ObjectName (tmp);
        }
        catch (MalformedObjectNameException e)
        {
          throw (new IllegalArgumentException ("Not a valid object name: "
                + tmp, e));
        }
      }
      else if (child.getKey ().equalsIgnoreCase ("Value"))
      {
        GenericJMXConfValue cv;

        cv = new GenericJMXConfValue (child);
        this._values.add (cv);
      }
      else
        throw (new IllegalArgumentException ("Unknown option: "
              + child.getKey ()));
    }

    if (this._obj_name == null)
      throw (new IllegalArgumentException ("No object name was defined."));

    if (this._values.size () == 0)
      throw (new IllegalArgumentException ("No value block was defined."));

  } /* }}} GenericJMXConfMBean (OConfigItem ci) */

  public String getName ()
  {
    return (this._name);
  }

  public void query (MBeanServerConnection conn, PluginData pd) /* {{{ */
  {
    pd.setPluginInstance ((this._instance != null) ? this._instance : "");

    for (int i = 0; i < this._values.size (); i++)
      this._values.get (i).query (conn, this._obj_name, pd);
  } /* }}} void query */
}

/* vim: set sw=2 sts=2 et fdm=marker : */
