/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/peter/Documents/pd-for-android/src/org/puredata/android/service/PdInterface.aidl
 */
package org.puredata.android.service;
public interface PdInterface extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.puredata.android.service.PdInterface
{
private static final java.lang.String DESCRIPTOR = "org.puredata.android.service.PdInterface";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.puredata.android.service.PdInterface interface,
 * generating a proxy if needed.
 */
public static org.puredata.android.service.PdInterface asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.puredata.android.service.PdInterface))) {
return ((org.puredata.android.service.PdInterface)iin);
}
return new org.puredata.android.service.PdInterface.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_openAudio:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
int _arg3;
_arg3 = data.readInt();
int _result = this.openAudio(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_closeAudio:
{
data.enforceInterface(DESCRIPTOR);
this.closeAudio();
reply.writeNoException();
return true;
}
case TRANSACTION_openPatch:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.openPatch(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_closePatch:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.closePatch(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_sendBang:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.sendBang(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_sendFloat:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
float _arg1;
_arg1 = data.readFloat();
int _result = this.sendFloat(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_sendSymbol:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _result = this.sendSymbol(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.puredata.android.service.PdInterface
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public int openAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sampleRate);
_data.writeInt(nIn);
_data.writeInt(nOut);
_data.writeInt(ticksPerBuffer);
mRemote.transact(Stub.TRANSACTION_openAudio, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void closeAudio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_closeAudio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public java.lang.String openPatch(java.lang.String patch, java.lang.String directory) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(patch);
_data.writeString(directory);
mRemote.transact(Stub.TRANSACTION_openPatch, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void closePatch(java.lang.String patch) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(patch);
mRemote.transact(Stub.TRANSACTION_closePatch, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public int sendBang(java.lang.String dest) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
mRemote.transact(Stub.TRANSACTION_sendBang, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int sendFloat(java.lang.String dest, float x) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeFloat(x);
mRemote.transact(Stub.TRANSACTION_sendFloat, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int sendSymbol(java.lang.String dest, java.lang.String symbol) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeString(symbol);
mRemote.transact(Stub.TRANSACTION_sendSymbol, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_openAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_closeAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_openPatch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_closePatch = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_sendBang = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_sendFloat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_sendSymbol = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public int openAudio(int sampleRate, int nIn, int nOut, int ticksPerBuffer) throws android.os.RemoteException;
public void closeAudio() throws android.os.RemoteException;
public java.lang.String openPatch(java.lang.String patch, java.lang.String directory) throws android.os.RemoteException;
public void closePatch(java.lang.String patch) throws android.os.RemoteException;
public int sendBang(java.lang.String dest) throws android.os.RemoteException;
public int sendFloat(java.lang.String dest, float x) throws android.os.RemoteException;
public int sendSymbol(java.lang.String dest, java.lang.String symbol) throws android.os.RemoteException;
}
