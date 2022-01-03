/*
 * ============================================================================
 *  Copyright © 2002-2020 by Thomas Thrien.
 *  All Rights Reserved.
 * ============================================================================
 *  Licensed to the public under the agreements of the GNU Lesser General Public
 *  License, version 3.0 (the "License"). You may obtain a copy of the License at
 *
 *       http://www.gnu.org/licenses/lgpl.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package org.tquadrat.foundation.tools;

import static java.lang.System.err;
import static java.lang.System.out;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.tquadrat.foundation.annotation.ClassVersion;
import org.tquadrat.foundation.annotation.PlaygroundClass;
import org.tquadrat.foundation.exception.PrivateConstructorForStaticClassCalledError;
import org.tquadrat.foundation.lang.StringConverter;

/**
 *  Tester for the retrieval of
 *  {@link StringConverter}
 *  instances from the service.
 *
 *  @extauthor Thomas Thrien - thomas.thrien@tquadrat.org
 *  @version $Id: ListStringConverters.java 820 2020-12-29 20:34:22Z tquadrat $
 *  @since 0.1.0
 *
 *  @UMLGraph.link
 */
@ClassVersion( sourceVersion = "$Id: ListStringConverters.java 820 2020-12-29 20:34:22Z tquadrat $" )
@API( status = EXPERIMENTAL, since = "0.1.0" )
@PlaygroundClass
public final class ListStringConverters
{
        /*--------------*\
    ====** Constructors **=====================================================
        \*--------------*/
    /**
     *  No instance allowed for this class.
     */
    private ListStringConverters() { throw new PrivateConstructorForStaticClassCalledError( ListStringConverters.class ); }

        /*---------*\
    ====** Methods **==========================================================
        \*---------*/
    /**
     * The program entry point.
     *
     * @param args The command line arguments.
     */
    public static final void main( final String... args )
    {
        try
        {
            StringConverter.list()
                .stream()
                .map( c -> c.getName() )
                .sorted()
                .forEach( out::println );
        }
        catch( final Throwable t )
        {
            //---* Handle previously unhandled exceptions *--------------------
            t.printStackTrace( err );
        }
    }  //  main()
}
//  class RetrieveStringConverter

/*
 *  End of File
 */