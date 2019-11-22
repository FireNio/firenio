/*
 * Copyright 2015 The FireNio Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.firenio.common.Cryptos;
import com.firenio.common.FileUtil;

/**
 * @author: wangkai
 **/
public class TestLoad1 {


    public static void main(String[] args) throws Exception {

        double dd = 10.55d;
        int aa = (int)dd;
        System.out.println(aa);

        String clazz = "yv66vgAAADQAYQoAGAA3CQANADgJAA0AOQcAOgoABAA7CQANADwHAD0LAD4APwoABwA7CwA+AEALAEEAQgsAQQBDBwBECgANAEULAD4ARgcARwoAEAA3CABICgAQAEkKABAASggASwoAEABMCgAQAE0HAE4BAA5tYXhfY29uY3VycmVudAEAAUkBAAdhdmdfcnR0AQABRAEABnBlcm1pdAEAIExqYXZhL3V0aWwvY29uY3VycmVudC9TZW1hcGhvcmU7AQAGPGluaXQ+AQAFKElEKVYBAARDb2RlAQAPTGluZU51bWJlclRhYmxlAQASTG9jYWxWYXJpYWJsZVRhYmxlAQAEdGhpcwEAIkxjb20vYWxpd2FyZS90aWFuY2hpL1Byb3ZpZGVyQ29uZjsBAAdjb252ZXJ0AQAiKExqYXZhL3V0aWwvTGlzdDspTGphdmEvdXRpbC9MaXN0OwEADHByb3ZpZGVyQ29uZgEAB2NvbmZpZ3MBABBMamF2YS91dGlsL0xpc3Q7AQAEY29uZgEAFkxvY2FsVmFyaWFibGVUeXBlVGFibGUBADRMamF2YS91dGlsL0xpc3Q8TGNvbS9hbGl3YXJlL3RpYW5jaGkvUHJvdmlkZXJDb25mOz47AQANU3RhY2tNYXBUYWJsZQcATwcAUAEACVNpZ25hdHVyZQEAaihMamF2YS91dGlsL0xpc3Q8TGNvbS9hbGl3YXJlL3RpYW5jaGkvUHJvdmlkZXJDb25mOz47KUxqYXZhL3V0aWwvTGlzdDxMY29tL2FsaXdhcmUvdGlhbmNoaS9Qcm92aWRlckNvbmY7PjsBAAh0b1N0cmluZwEAFCgpTGphdmEvbGFuZy9TdHJpbmc7AQAKU291cmNlRmlsZQEAEVByb3ZpZGVyQ29uZi5qYXZhDAAfAFEMABkAGgwAGwAcAQAeamF2YS91dGlsL2NvbmN1cnJlbnQvU2VtYXBob3JlDAAfAFIMAB0AHgEAE2phdmEvdXRpbC9BcnJheUxpc3QHAE8MAFMAVAwAVQBWBwBQDABXAFgMAFkAWgEAIGNvbS9hbGl3YXJlL3RpYW5jaGkvUHJvdmlkZXJDb25mDAAfACAMAFsAXAEAF2phdmEvbGFuZy9TdHJpbmdCdWlsZGVyAQAOY29uZjogYXZnX3J0dDoMAF0AXgwAXQBfAQARLCBtYXhfY29uY3VycmVudDoMAF0AYAwAMwA0AQAQamF2YS9sYW5nL09iamVjdAEADmphdmEvdXRpbC9MaXN0AQASamF2YS91dGlsL0l0ZXJhdG9yAQADKClWAQAEKEkpVgEABHNpemUBAAMoKUkBAAhpdGVyYXRvcgEAFigpTGphdmEvdXRpbC9JdGVyYXRvcjsBAAdoYXNOZXh0AQADKClaAQAEbmV4dAEAFCgpTGphdmEvbGFuZy9PYmplY3Q7AQADYWRkAQAVKExqYXZhL2xhbmcvT2JqZWN0OylaAQAGYXBwZW5kAQAtKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZ0J1aWxkZXI7AQAcKEQpTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwEAHChJKUxqYXZhL2xhbmcvU3RyaW5nQnVpbGRlcjsAIQANABgAAAADABEAGQAaAAAAEQAbABwAAAARAB0AHgAAAAMAAQAfACAAAQAhAAAAaQAEAAQAAAAbKrcAASobtQACKii1AAMquwAEWRu3AAW1AAaxAAAAAgAiAAAAFgAFAAAADwAEABAACQARAA4AEgAaABMAIwAAACAAAwAAABsAJAAlAAAAAAAbABkAGgABAAAAGwAbABwAAgAJACYAJwACACEAAADBAAYABAAAAEO7AAdZKrkACAEAtwAJTCq5AAoBAE0suQALAQCZACYsuQAMAQDAAA1OK7sADVkttAACLbQAA7cADrkADwIAV6f/1yuwAAAABAAiAAAAFgAFAAAAFgAOABcAKAAYAD4AGQBBABoAIwAAACAAAwAoABYAKAAlAAMAAABDACkAKgAAAA4ANQArACoAAQAsAAAAFgACAAAAQwApAC0AAAAOADUAKwAtAAEALgAAAA4AAv0AFQcALwcAMPoAKwAxAAAAAgAyAAEAMwA0AAEAIQAAAE0AAwABAAAAI7sAEFm3ABESErYAEyq0AAO2ABQSFbYAEyq0AAK2ABa2ABewAAAAAgAiAAAABgABAAAAHwAjAAAADAABAAAAIwAkACUAAAABADUAAAACADY=";

        FileUtil.writeByFile(new File("C://temp/a.class"), Cryptos.base64_de(clazz));


    }


}
