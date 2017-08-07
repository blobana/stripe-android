package com.stripe.android.view;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stripe.android.R;
import com.stripe.android.model.Card;
import com.stripe.android.model.CustomerSource;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceCardData;

import java.util.HashMap;
import java.util.Map;

import static com.stripe.android.view.ViewUtils.getThemeAccentColor;
import static com.stripe.android.view.ViewUtils.getThemeColorControlNormal;

public class MaskedCardView extends LinearLayout {

    private @Card.CardBrand String mCardBrand;
    private String mLast4;
    private boolean mIsSelected;

    private ImageView mCardIconImageView;
    private TextView mCardInformationTextView;
    private ImageView mSelectedImageView;

    @ColorInt private int mSelectedColorId;
    @ColorInt private int mUnselectedColorId;

    static final Map<String , Integer> TEMPLATE_RESOURCE_MAP = new HashMap<>();
    static {
        TEMPLATE_RESOURCE_MAP.put(Card.AMERICAN_EXPRESS, R.drawable.ic_amex_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.DINERS_CLUB, R.drawable.ic_diners_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.DISCOVER, R.drawable.ic_discover_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.JCB, R.drawable.ic_jcb_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.MASTERCARD, R.drawable.ic_mastercard_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.VISA, R.drawable.ic_visa_template_32);
        TEMPLATE_RESOURCE_MAP.put(Card.UNKNOWN, R.drawable.ic_unknown);
    }

    public MaskedCardView(Context context) {
        super(context);
        init();
    }

    public MaskedCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskedCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    void init() {
        inflate(getContext(), R.layout.masked_card_view, this);
        setOrientation(HORIZONTAL);
        setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.card_widget_min_width));
        int paddingPixels = getContext()
                .getResources().getDimensionPixelSize(R.dimen.masked_card_vertical_padding);
        setPadding(0, paddingPixels, 0, paddingPixels);

        mCardIconImageView = findViewById(R.id.masked_icon_view);
        mCardInformationTextView = findViewById(R.id.masked_card_info_view);
        mSelectedImageView = findViewById(R.id.masked_selected_icon);

        mSelectedColorId = getThemeAccentColor(getContext()).data;
        mUnselectedColorId = getThemeColorControlNormal(getContext()).data;

        initializeCheckMark();
    }

    public void setCard(@NonNull Card card) {
        mCardBrand = card.getBrand();
        mLast4 = card.getLast4();
        updateBrandIcon();
        updateCardInformation();
    }

    public void setCardData(@NonNull SourceCardData sourceCardData) {
        mCardBrand = sourceCardData.getBrand();
        mLast4 = sourceCardData.getLast4();
        updateBrandIcon();
        updateCardInformation();
    }

    public void setCustomerSource(@NonNull CustomerSource customerSource) {
        Source source = customerSource.asSource();
        if (source != null
                && source.getSourceTypeModel() != null
                && Source.CARD.equals(source.getType())
                && source.getSourceTypeModel() instanceof SourceCardData) {
            SourceCardData sourceCardData = (SourceCardData) source.getSourceTypeModel();
            setCardData(sourceCardData);
        }

        Card card = customerSource.asCard();
        if (card != null) {
            setCard(card);
        }
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
        updateCheckMark();
        updateBrandIcon();
        updateTextColor();
    }

    public void toggleSelected() {
        setIsSelected(!mIsSelected);
    }

    private void initializeCheckMark() {
        updateDrawable(R.drawable.ic_checkmark, mSelectedImageView, true);
    }

    private void updateBrandIcon() {
        @DrawableRes int iconResourceId = TEMPLATE_RESOURCE_MAP.get(mCardBrand);
        updateDrawable(iconResourceId, mCardIconImageView, false);
    }

    private void updateTextColor() {
        @ColorInt int textColor = mIsSelected ? mSelectedColorId : mUnselectedColorId;
        mCardInformationTextView.setTextColor(textColor);
    }

    @SuppressWarnings("deprecation")
    private void updateDrawable(
            @DrawableRes int resourceId,
            @NonNull ImageView imageView,
            boolean isCheckMark) {
        Drawable icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = getResources().getDrawable(resourceId, imageView.getContext().getTheme());
        } else {
            // This method still triggers the "deprecation" warning, despite the other
            // one not being allowed for SDK < 21
            icon = getResources().getDrawable(resourceId);
        }

        @ColorInt int tintColor = mIsSelected || isCheckMark ? mSelectedColorId : mUnselectedColorId;
        Drawable compatIcon = DrawableCompat.wrap(icon);
        DrawableCompat.setTint(compatIcon.mutate(), tintColor);
        imageView.setImageDrawable(compatIcon);
    }

    private void updateCardInformation() {
        String brandText = Card.AMERICAN_EXPRESS.equals(mCardBrand)
                ? getResources().getString(R.string.amex_short)
                : mCardBrand;
        String normalText = getResources().getString(R.string.ending_in);
        int brandLength = brandText.length();
        int middleLength = normalText.length();
        int last4length = mLast4.length();
        SpannableString str = new SpannableString(brandText + normalText + mLast4);
        str.setSpan(
                new StyleSpan(Typeface.BOLD),
                0,
                brandLength,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        str.setSpan(new StyleSpan(Typeface.BOLD),
                brandLength + middleLength,
                brandLength + middleLength + last4length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mCardInformationTextView.setText(str);
    }

    private void updateCheckMark() {
        if (mIsSelected) {
            mSelectedImageView.setVisibility(View.VISIBLE);
        } else {
            mSelectedImageView.setVisibility(View.INVISIBLE);
        }
    }
}
