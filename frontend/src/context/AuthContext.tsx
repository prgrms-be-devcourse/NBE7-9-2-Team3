'use client';

import React, { createContext, useContext, useState, ReactNode, useEffect } from 'react';
import { User, LoginRequest, SignupRequest, LoginResponse, SignupResponse, ApiResponse } from '@/type/user';
import { fetchApi } from '@/lib/client';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  signup: (email: string, password: string, nickname: string, profileImage?: string) => Promise<void>;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const isAuthenticated = !!user;

  // 컴포넌트 마운트 시 사용자 정보 확인 (선택적)
  useEffect(() => {
    // 로그인 상태 확인을 시도하되, 실패해도 앱이 정상 작동하도록 함
    checkAuthStatus().catch(() => {
      // 조용히 실패 처리
    });
  }, []);

  const checkAuthStatus = async () => {
    try {
      const data: ApiResponse<User> = await fetchApi('/api/members/me');
      if (data.data) {
        setUser(data.data);
      }
    } catch (error) {
      // 네트워크 오류나 서버 연결 실패는 조용히 처리
      if (error instanceof Error && (
        error.message.includes('Failed to fetch') || 
        error.message.includes('NetworkError') ||
        error.message.includes('fetch')
      )) {
        console.warn('서버 연결을 확인할 수 없습니다. 로그인이 필요할 수 있습니다.');
      } else {
        console.error('Auth check failed:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  const login = async (email: string, password: string) => {
    setLoading(true);
    try {
      const loginData: LoginRequest = { email, password };
      const data: ApiResponse<LoginResponse> = await fetchApi('/api/members/login', {
        method: 'POST',
        body: JSON.stringify(loginData),
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
        });
      }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const signup = async (email: string, password: string, nickname: string, profileImage?: string) => {
    setLoading(true);
    try {
      const signupData: SignupRequest = { 
        email, 
        password, 
        nickname, 
        profileImage: profileImage || undefined 
      };
      
      const data: ApiResponse<SignupResponse> = await fetchApi('/api/members/join', {
        method: 'POST',
        body: JSON.stringify(signupData),
      });

      if (data.data) {
        setUser({
          memberId: data.data.memberId,
          email: data.data.email,
          nickname: data.data.nickname,
          profileImage: data.data.profileImage,
        });
      }
    } catch (error) {
      console.error('Signup error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    setLoading(true);
    try {
      await fetchApi('/api/members/logout', {
        method: 'POST',
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
      setLoading(false);
    }
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    login,
    logout,
    signup,
    loading,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
