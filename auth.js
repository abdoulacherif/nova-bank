// auth.js - Fichier centralisé pour la gestion de l'authentification NOVA BANK
// Version adaptée à la nouvelle base de données

// Configuration Supabase (NOUVELLES CLÉS)
const SUPABASE_URL = 'https://lvbdafzdsuotairqkcnv.supabase.co';
const SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx2YmRhZnpkc3VvdGFpcnFrY252Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzEwNTg0MTUsImV4cCI6MjA4NjYzNDQxNX0.MWPo0ZnTNTzK4_8N1GB_mtri7LkgfP43W4gjBGB7zhI';

// Initialisation de Supabase
const { createClient } = supabase;
const novabank = createClient(SUPABASE_URL, SUPABASE_KEY);

// ==================== GESTION DES UTILISATEURS ====================

/**
 * Inscription d'un nouvel utilisateur
 * @param {Object} userData - Données de l'utilisateur
 * @param {string} userData.nom - Nom complet
 * @param {string} userData.email - Email
 * @param {string} userData.password - Mot de passe
 * @returns {Promise<Object>} - Résultat de l'inscription
 */
async function registerUser(userData) {
    try {
        const { nom, email, password } = userData;
        
        // Inscription avec Supabase
        const { data, error } = await novabank.auth.signUp({
            email: email,
            password: password,
            options: {
                data: {
                    nom: nom
                }
            }
        });

        if (error) throw error;

        if (data && data.user) {
            // Attendre que le profil soit créé
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            return {
                success: true,
                user: data.user,
                message: 'Inscription réussie'
            };
        }

        return {
            success: false,
            message: 'Erreur lors de l\'inscription'
        };

    } catch (error) {
        console.error('Erreur registerUser:', error);
        
        let message = error.message;
        if (error.message.includes('User already registered')) {
            message = 'Un compte existe déjà avec cet email';
        } else if (error.message.includes('password')) {
            message = 'Le mot de passe doit contenir au moins 6 caractères';
        }
        
        return {
            success: false,
            message: message
        };
    }
}

/**
 * Connexion d'un utilisateur
 * @param {string} email - Email de l'utilisateur
 * @param {string} password - Mot de passe
 * @returns {Promise<Object>} - Résultat de la connexion
 */
async function loginUser(email, password) {
    try {
        const { data, error } = await novabank.auth.signInWithPassword({
            email: email,
            password: password
        });

        if (error) throw error;

        if (data && data.user) {
            return {
                success: true,
                user: data.user,
                session: data.session,
                message: 'Connexion réussie'
            };
        }

        return {
            success: false,
            message: 'Erreur lors de la connexion'
        };

    } catch (error) {
        console.error('Erreur loginUser:', error);
        
        let message = 'Erreur lors de la connexion';
        if (error.message.includes('Invalid login credentials')) {
            message = 'Email ou mot de passe incorrect';
        } else if (error.message.includes('Email not confirmed')) {
            message = 'Veuillez confirmer votre email';
        } else if (error.message.includes('rate limit')) {
            message = 'Trop de tentatives, réessayez plus tard';
        }
        
        return {
            success: false,
            message: message
        };
    }
}

/**
 * Déconnexion de l'utilisateur
 * @returns {Promise<Object>} - Résultat de la déconnexion
 */
async function logoutUser() {
    try {
        const { error } = await novabank.auth.signOut();
        if (error) throw error;
        
        return {
            success: true,
            message: 'Déconnexion réussie'
        };
    } catch (error) {
        console.error('Erreur logoutUser:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors de la déconnexion'
        };
    }
}

// ==================== GESTION DES PROFILS ====================

/**
 * Récupérer le profil de l'utilisateur connecté
 * @returns {Promise<Object>} - Profil utilisateur
 */
async function getUserProfile() {
    try {
        const { data: { user }, error: userError } = await novabank.auth.getUser();
        
        if (userError) throw userError;
        if (!user) throw new Error('Utilisateur non connecté');

        const { data, error } = await novabank
            .from('profiles')
            .select('*')
            .eq('id', user.id)
            .single();

        if (error) throw error;

        return {
            success: true,
            profile: data
        };

    } catch (error) {
        console.error('Erreur getUserProfile:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors du chargement du profil'
        };
    }
}

/**
 * Mettre à jour le profil utilisateur
 * @param {Object} profileData - Données à mettre à jour
 * @returns {Promise<Object>} - Résultat de la mise à jour
 */
async function updateUserProfile(profileData) {
    try {
        const { data: { user }, error: userError } = await novabank.auth.getUser();
        
        if (userError) throw userError;
        if (!user) throw new Error('Utilisateur non connecté');

        const { data, error } = await novabank
            .from('profiles')
            .update(profileData)
            .eq('id', user.id)
            .select()
            .single();

        if (error) throw error;

        return {
            success: true,
            profile: data,
            message: 'Profil mis à jour'
        };

    } catch (error) {
        console.error('Erreur updateUserProfile:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors de la mise à jour'
        };
    }
}

/**
 * Mettre à jour le solde d'un utilisateur (pour les admins)
 * @param {string} userId - ID de l'utilisateur
 * @param {number} newBalance - Nouveau solde
 * @returns {Promise<Object>} - Résultat de la mise à jour
 */
async function updateUserBalance(userId, newBalance) {
    try {
        const { error } = await novabank
            .from('profiles')
            .update({ solde: newBalance })
            .eq('id', userId);

        if (error) throw error;

        return {
            success: true,
            message: 'Solde mis à jour avec succès'
        };

    } catch (error) {
        console.error('Erreur updateUserBalance:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors de la mise à jour du solde'
        };
    }
}

// ==================== GESTION DES UTILISATEURS (ADMIN) ====================

/**
 * Récupérer tous les utilisateurs (pour les admins)
 * @returns {Promise<Object>} - Liste des utilisateurs
 */
async function getAllUsers() {
    try {
        const { data, error } = await novabank
            .from('profiles')
            .select('*')
            .order('created_at', { ascending: false });

        if (error) throw error;

        return {
            success: true,
            users: data
        };

    } catch (error) {
        console.error('Erreur getAllUsers:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors du chargement des utilisateurs'
        };
    }
}

/**
 * Supprimer un utilisateur (pour les admins)
 * @param {string} userId - ID de l'utilisateur
 * @returns {Promise<Object>} - Résultat de la suppression
 */
async function deleteUser(userId) {
    try {
        const { error } = await novabank
            .from('profiles')
            .delete()
            .eq('id', userId);

        if (error) throw error;

        return {
            success: true,
            message: 'Utilisateur supprimé avec succès'
        };

    } catch (error) {
        console.error('Erreur deleteUser:', error);
        return {
            success: false,
            message: error.message || 'Erreur lors de la suppression'
        };
    }
}

// ==================== UTILITAIRES ====================

/**
 * Vérifier si l'utilisateur est connecté
 * @returns {Promise<boolean>}
 */
async function isAuthenticated() {
    try {
        const { data: { user } } = await novabank.auth.getUser();
        return !!user;
    } catch {
        return false;
    }
}

/**
 * Récupérer l'utilisateur connecté
 * @returns {Promise<Object|null>}
 */
async function getCurrentUser() {
    try {
        const { data: { user } } = await novabank.auth.getUser();
        return user;
    } catch {
        return null;
    }
}

/**
 * Rediriger si non connecté
 * @param {string} redirectUrl - URL de redirection
 */
async function requireAuth(redirectUrl = 'connexion.html') {
    const authenticated = await isAuthenticated();
    if (!authenticated) {
        window.location.href = redirectUrl;
    }
}

/**
 * Rediriger si déjà connecté
 * @param {string} redirectUrl - URL de redirection
 */
async function redirectIfAuthenticated(redirectUrl = 'dashboard.html') {
    const authenticated = await isAuthenticated();
    if (authenticated) {
        window.location.href = redirectUrl;
    }
}

/**
 * Calculer la force du mot de passe
 * @param {string} password - Mot de passe
 * @returns {Object} - Force et couleur
 */
function checkPasswordStrength(password) {
    let strength = 0;
    
    if (password.length >= 8) strength += 20;
    if (password.match(/[a-z]/)) strength += 20;
    if (password.match(/[A-Z]/)) strength += 20;
    if (password.match(/[0-9]/)) strength += 20;
    if (password.match(/[^a-zA-Z0-9]/)) strength += 20;

    let text = 'Très faible';
    let color = '#ff6b4a';
    
    if (strength <= 20) {
        text = 'Très faible';
        color = '#ff6b4a';
    } else if (strength <= 40) {
        text = 'Faible';
        color = '#ff9f4a';
    } else if (strength <= 60) {
        text = 'Moyen';
        color = '#ff9f4a';
    } else if (strength <= 80) {
        text = 'Bon';
        color = '#34a853';
    } else {
        text = 'Excellent';
        color = '#34a853';
    }

    return {
        strength: strength,
        text: text,
        color: color
    };
}

/**
 * Formater un montant en euros
 * @param {number} amount - Montant à formater
 * @returns {string} - Montant formaté
 */
function formatAmount(amount) {
    return amount.toFixed(2).replace('.', ',') + ' €';
}

// ==================== EXPORTS ====================

// Exporter toutes les fonctions
window.NOVABANK = {
    // Authentification
    register: registerUser,
    login: loginUser,
    logout: logoutUser,
    
    // Profils
    getProfile: getUserProfile,
    updateProfile: updateUserProfile,
    updateBalance: updateUserBalance,
    
    // Admin
    getAllUsers: getAllUsers,
    deleteUser: deleteUser,
    
    // Utilitaires
    isAuthenticated: isAuthenticated,
    getCurrentUser: getCurrentUser,
    requireAuth: requireAuth,
    redirectIfAuthenticated: redirectIfAuthenticated,
    checkPasswordStrength: checkPasswordStrength,
    formatAmount: formatAmount,
    
    // Constantes
    supabase: novabank
};

console.log('✅ Auth.js chargé - NOVA BANK (nouvelle version)');