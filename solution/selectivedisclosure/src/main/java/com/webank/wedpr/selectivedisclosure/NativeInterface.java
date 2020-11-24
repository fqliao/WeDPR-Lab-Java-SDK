// Copyright 2020 WeDPR Lab Project Authors. Licensed under Apache-2.0.

package com.webank.wedpr.selectivedisclosure;

import com.webank.wedpr.common.NativeUtils;
import com.webank.wedpr.common.WedprException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/** Native interface for selective disclosure client. */
public class NativeInterface {

  public static String WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH;
  public static String LIBSSL_1_1 = "libssl.so.1.1";
  public static String LIBSSL_1_0 = "libssl.so.10";

  static {
    try {
      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.contains("windows")) {
        WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH = "/WeDPR_dynamic_lib/ffi_selective_disclosure.dll";
        NativeUtils.loadLibraryFromJar("/WeDPR_dynamic_lib/libeay32md.dll");
        NativeUtils.loadLibraryFromJar("/WeDPR_dynamic_lib/ssleay32md.dll");
      } else if (osName.contains("linux")) {
        if (hasLibsslVersion(LIBSSL_1_0)) {
          WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH =
              "/WeDPR_dynamic_lib/libffi_selective_disclosure_libssl_1_0.so";
        } else if (hasLibsslVersion(LIBSSL_1_1)) {
          WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH =
              "/WeDPR_dynamic_lib/libffi_selective_disclosure_libssl_1_1.so";
        } else {
          throw new WedprException("The Linux needs " + LIBSSL_1_1 + " or " + LIBSSL_1_0 + ".");
        }

      } else if (osName.contains("mac")) {
        WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH =
            "/WeDPR_dynamic_lib/libffi_selective_disclosure.dylib";
      } else {
        throw new WedprException("Unsupported the os " + osName + ".");
      }
      NativeUtils.loadLibraryFromJar(WEDPR_SELECTIVE_DISCLOSURE_LIB_PATH);

    } catch (IOException | WedprException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean hasLibsslVersion(String libsslVersion)
      throws IOException, UnsupportedEncodingException {
    Process process = Runtime.getRuntime().exec("locate " + libsslVersion);
    InputStream inputStream = process.getInputStream();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    String version = bufferedReader.readLine();
    return version != null;
  }

  // JNI function section.
  public static native IssuerResult issuerMakeCredentialTemplate(String attributeTemplate);

  public static native IssuerResult issuerSignCredential(
      String credentialTemplate,
      String templateSecretKey,
      String credentialRequest,
      String userId,
      String nonce);

  public static native UserResult userMakeCredential(
      String credentialInfo, String credentialTemplate);

  public static native UserResult userBlindCredentialSignature(
      String credentialSignature,
      String credentialInfo,
      String credentialTemplate,
      String masterSecret,
      String credentialSecretsBlindingFactors,
      String nonceCredential);

  public static native UserResult userProveCredentialInfo(
      String verificationPredicateRule,
      String credentialSignature,
      String credentialInfo,
      String credentialTemplate,
      String masterSecret);

  public static native VerifierResult verifierVerifyProof(
      String verificationPredicateRule, String verificationRequest);

  public static native VerifierResult verifierGetRevealedAttrsFromVerificationRequest(
      String verificationRequest);
}
