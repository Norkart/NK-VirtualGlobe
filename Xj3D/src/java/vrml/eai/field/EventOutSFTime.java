/******************************************************************************
 *
 *                      VRML Browser basic classes
 *                   For External Authoring Interface
 *
 *                   (C) 1998 Justin Couch
 *
 *  Written by Justin Couch: justin@vlc.com.au
 *
 * This code is free software and is distributed under the terms implied by
 * the GNU LGPL. A full version of this license can be found at
 * http://www.gnu.org/copyleft/lgpl.html
 *
 *****************************************************************************/

package vrml.eai.field;

/**
 * VRML eventOut class for SFTime.
 * <P>
 * Time values are represented as per the VRML IS specification Section
 * 4.11 Time. That is, time is set as VRML "Time" - the number of seconds since
 * Jan 1, 1970 GMT, rather than a Java time which is a long, the number
 * of milliseconds since Jan 1, 1970 GMT. To convert between the two simply
 * divide java time by 1000 and cast to a double.
 * <P>
 * Note that in setting time values from an external application, the idea of
 * the time that java represents and the time that the VRML world currently
 * has set may well be different. It is best to source the current "time" from
 * a node or eventOut in the VRML world rather than relying exclusively on
 * the value returned from <CODE>System.currentTimeMillies</CODE>. This is
 * especially important to note if you are dealing with high speed, narrow
 * interval work such as controlling animation.
 *
 * @version 1.0 30 April 1998
 */
public abstract class EventOutSFTime extends EventOut
{
  /**
   * Construct an instance of this class. Calls the superclass constructor
   * with the field type set to SFTime.
   */
  protected EventOutSFTime()
  {
    super(SFTime);
  }

  /**
   * Get the time value in the given eventOut. Time can be any value either
   * positive or negative but always absolute in value. As per the VRML
   * time specification, all time values are absolute.
   *
   * @return The current time of this eventOut.
   */
  public abstract double getValue();
}









