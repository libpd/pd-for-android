/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/peter/Documents/pd-for-android-common/src/org/puredata/android/service/IPdService.aidl
 */
package org.puredata.android.service;
public interface IPdService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.puredata.android.service.IPdService
{
private static final java.lang.String DESCRIPTOR = "org.puredata.android.service.IPdService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.puredata.android.service.IPdService interface,
 * generating a proxy if needed.
 */
public static org.puredata.android.service.IPdService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.puredata.android.service.IPdService))) {
return ((org.puredata.android.service.IPdService)iin);
}
return new org.puredata.android.service.IPdService.Stub.Proxy(obj);
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
case TRANSACTION_addClient:
{
data.enforceInterface(DESCRIPTOR);
org.puredata.android.service.IPdClient _arg0;
_arg0 = org.puredata.android.service.IPdClient.Stub.asInterface(data.readStrongBinder());
this.addClient(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeClient:
{
data.enforceInterface(DESCRIPTOR);
org.puredata.android.service.IPdClient _arg0;
_arg0 = org.puredata.android.service.IPdClient.Stub.asInterface(data.readStrongBinder());
this.removeClient(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_requestAudio:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
int _arg2;
_arg2 = data.readInt();
float _arg3;
_arg3 = data.readFloat();
int _result = this.requestAudio(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_releaseAudio:
{
data.enforceInterface(DESCRIPTOR);
this.releaseAudio();
reply.writeNoException();
return true;
}
case TRANSACTION_stop:
{
data.enforceInterface(DESCRIPTOR);
this.stop();
reply.writeNoException();
return true;
}
case TRANSACTION_isRunning:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRunning();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getSampleRate:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSampleRate();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getInputChannels:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getInputChannels();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getOutputChannels:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getOutputChannels();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getBufferSizeMillis:
{
data.enforceInterface(DESCRIPTOR);
float _result = this.getBufferSizeMillis();
reply.writeNoException();
reply.writeFloat(_result);
return true;
}
case TRANSACTION_exists:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.exists(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_subscribe:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.puredata.android.service.IPdListener _arg1;
_arg1 = org.puredata.android.service.IPdListener.Stub.asInterface(data.readStrongBinder());
this.subscribe(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_unsubscribe:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.puredata.android.service.IPdListener _arg1;
_arg1 = org.puredata.android.service.IPdListener.Stub.asInterface(data.readStrongBinder());
this.unsubscribe(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_sendBang:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.sendBang(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_sendFloat:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
float _arg1;
_arg1 = data.readFloat();
this.sendFloat(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_sendSymbol:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.sendSymbol(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_sendList:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.List _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readArrayList(cl);
this.sendList(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_sendMessage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.util.List _arg2;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readArrayList(cl);
this.sendMessage(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.puredata.android.service.IPdService
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
/**
	 * subscribe to updates on audio status
	 */
public void addClient(org.puredata.android.service.IPdClient client) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((client!=null))?(client.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_addClient, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * cancel subscription
	 */
public void removeClient(org.puredata.android.service.IPdClient client) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((client!=null))?(client.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_removeClient, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * request audio with the given parameters; starts or restarts the audio thread if necessary
	 *
	 * returns an error code, 0 on success
	 */
public int requestAudio(int sampleRate, int inputChannels, int outputChannels, float bufferSizeMillis) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(sampleRate);
_data.writeInt(inputChannels);
_data.writeInt(outputChannels);
_data.writeFloat(bufferSizeMillis);
mRemote.transact(Stub.TRANSACTION_requestAudio, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * indicates that this client no longer needs the audio thread; stops the audio thread if no clients are left
	 */
public void releaseAudio() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_releaseAudio, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * attempt to stop pd service; success depends on cooperation of all clients bound to the service
	 */
public void stop() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * indicates whether the audio thread is running
	 */
public boolean isRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRunning, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getSampleRate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSampleRate, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getInputChannels() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getInputChannels, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public int getOutputChannels() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getOutputChannels, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public float getBufferSizeMillis() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
float _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getBufferSizeMillis, _data, _reply, 0);
_reply.readException();
_result = _reply.readFloat();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * checks whether a symbol refers to something in pd
	 */
public boolean exists(java.lang.String symbol) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(symbol);
mRemote.transact(Stub.TRANSACTION_exists, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * subscribe to messages sent to a symbol in pd
	 */
public void subscribe(java.lang.String symbol, org.puredata.android.service.IPdListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(symbol);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_subscribe, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * unsubscribe from messages sent to a symbol in pd
	 */
public void unsubscribe(java.lang.String symbol, org.puredata.android.service.IPdListener listener) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(symbol);
_data.writeStrongBinder((((listener!=null))?(listener.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unsubscribe, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendBang(java.lang.String dest) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
mRemote.transact(Stub.TRANSACTION_sendBang, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendFloat(java.lang.String dest, float x) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeFloat(x);
mRemote.transact(Stub.TRANSACTION_sendFloat, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendSymbol(java.lang.String dest, java.lang.String symbol) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeString(symbol);
mRemote.transact(Stub.TRANSACTION_sendSymbol, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendList(java.lang.String dest, java.util.List args) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeList(args);
mRemote.transact(Stub.TRANSACTION_sendList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void sendMessage(java.lang.String dest, java.lang.String symbol, java.util.List args) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dest);
_data.writeString(symbol);
_data.writeList(args);
mRemote.transact(Stub.TRANSACTION_sendMessage, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_addClient = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_removeClient = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_requestAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_releaseAudio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_stop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_isRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getSampleRate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getInputChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getOutputChannels = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getBufferSizeMillis = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_exists = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_subscribe = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_unsubscribe = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_sendBang = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_sendFloat = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_sendSymbol = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_sendList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_sendMessage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
}
/**
	 * subscribe to updates on audio status
	 */
public void addClient(org.puredata.android.service.IPdClient client) throws android.os.RemoteException;
/**
	 * cancel subscription
	 */
public void removeClient(org.puredata.android.service.IPdClient client) throws android.os.RemoteException;
/**
	 * request audio with the given parameters; starts or restarts the audio thread if necessary
	 *
	 * returns an error code, 0 on success
	 */
public int requestAudio(int sampleRate, int inputChannels, int outputChannels, float bufferSizeMillis) throws android.os.RemoteException;
/**
	 * indicates that this client no longer needs the audio thread; stops the audio thread if no clients are left
	 */
public void releaseAudio() throws android.os.RemoteException;
/**
	 * attempt to stop pd service; success depends on cooperation of all clients bound to the service
	 */
public void stop() throws android.os.RemoteException;
/**
	 * indicates whether the audio thread is running
	 */
public boolean isRunning() throws android.os.RemoteException;
public int getSampleRate() throws android.os.RemoteException;
public int getInputChannels() throws android.os.RemoteException;
public int getOutputChannels() throws android.os.RemoteException;
public float getBufferSizeMillis() throws android.os.RemoteException;
/**
	 * checks whether a symbol refers to something in pd
	 */
public boolean exists(java.lang.String symbol) throws android.os.RemoteException;
/**
	 * subscribe to messages sent to a symbol in pd
	 */
public void subscribe(java.lang.String symbol, org.puredata.android.service.IPdListener listener) throws android.os.RemoteException;
/**
	 * unsubscribe from messages sent to a symbol in pd
	 */
public void unsubscribe(java.lang.String symbol, org.puredata.android.service.IPdListener listener) throws android.os.RemoteException;
public void sendBang(java.lang.String dest) throws android.os.RemoteException;
public void sendFloat(java.lang.String dest, float x) throws android.os.RemoteException;
public void sendSymbol(java.lang.String dest, java.lang.String symbol) throws android.os.RemoteException;
public void sendList(java.lang.String dest, java.util.List args) throws android.os.RemoteException;
public void sendMessage(java.lang.String dest, java.lang.String symbol, java.util.List args) throws android.os.RemoteException;
}
